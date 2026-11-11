const SESSION_KEY = 'gamersbusiness.session';

export function getSession() {
  try {
    const value = JSON.parse(sessionStorage.getItem(SESSION_KEY));
    if (value?.accessToken && Date.parse(value.expiresAt) > Date.now()) return value;
  } catch { /* A malformed or unavailable browser session is not authenticated. */ }
  clearSession();
  return null;
}
export function saveSession(session) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
}
export function clearSession() { sessionStorage.removeItem(SESSION_KEY); }

export class ApiError extends Error {
  constructor(message, status, errors) {
    super(message);
    this.status = status;
    this.errors = errors;
  }
}

export async function api(path, { method = 'GET', body, anonymous = false, signal } = {}) {
  const session = anonymous ? null : getSession();
  if (!anonymous && !session) {
    window.dispatchEvent(new Event('gb:session-expired'));
    throw new ApiError('Sua sessão expirou. Entre novamente para continuar.', 401);
  }
  let response;
  try {
    response = await fetch(`/api/v1${path}`, {
      method, signal,
      headers: { ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}), ...(session ? { Authorization: `Bearer ${session.accessToken}` } : {}) },
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.', 0);
  }
  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (!response.ok) {
    if (response.status === 401 && !anonymous) {
      clearSession();
      window.dispatchEvent(new Event('gb:session-expired'));
    }
    const errors = data?.errors;
    const details = Array.isArray(errors) ? errors.join(' · ') : errors && typeof errors === 'object' ? Object.entries(errors).map(([key, val]) => `${key}: ${val}`).join(' · ') : errors;
    throw new ApiError([data?.message || `Não foi possível concluir a solicitação (${response.status}).`, details].filter(Boolean).join(' — '), response.status, errors);
  }
  return data;
}

// Academic datasets fit in memory; every server page is read, without silently truncating at 100.
export async function allPages(path, options = {}) {
  const separator = path.includes('?') ? '&' : '?';
  const first = await api(`${path}${separator}page=0&size=100`, options);
  const items = [...first.items];
  for (let page = 1; page < first.totalPages; page++) {
    const next = await api(`${path}${separator}page=${page}&size=100`, options);
    items.push(...next.items);
  }
  return { ...first, items };
}
