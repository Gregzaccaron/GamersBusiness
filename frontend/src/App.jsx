import { useCallback, useEffect, useRef, useState } from 'react';
import { ArrowUpRight, Compass, Gamepad2, LibraryBig, LogIn, LogOut, Menu, ShieldCheck, Trophy, UserRound, X } from 'lucide-react';
import { allPages, api, clearSession, getSession } from './api';
import { AuthModal, Welcome } from './components/Auth';
import { Store } from './components/Store';
import { GameDetail } from './components/GameDetail';
import { Achievements, Library, Profile } from './components/Personal';
import { Admin } from './components/Admin';
import { Button, ErrorBox, Loading, Toasts } from './components/UI';

const emptyData = { games: [], categories: [], developers: [], achievements: [], library: [], unlocked: [], users: [] };
const navigation = [
  { id: 'store', label: 'Descobrir', icon: Compass },
  { id: 'library', label: 'Minha biblioteca', icon: LibraryBig },
  { id: 'achievements', label: 'Conquistas', icon: Trophy },
  { id: 'profile', label: 'Meu perfil', icon: UserRound },
];
function initialPage() { const value = window.location.hash.slice(1); return [...navigation.map(item => item.id), 'admin'].includes(value) ? value : 'store'; }

export default function App() {
  const [session, setSession] = useState(getSession);
  const [user, setUser] = useState(null);
  const [data, setData] = useState(emptyData);
  const [loading, setLoading] = useState(Boolean(session));
  const [error, setError] = useState(null);
  const [authMode, setAuthMode] = useState(null);
  const [page, setPage] = useState(initialPage);
  const [selectedGame, setSelectedGame] = useState(null);
  const [mobileMenu, setMobileMenu] = useState(false);
  const [toasts, setToasts] = useState([]);
  const timers = useRef(new Set());
  const notify = useCallback((message, error = false) => {
    const id = crypto.randomUUID();
    setToasts(items => [...items.slice(-3), { id, message, error }]);
    const timer = setTimeout(() => { setToasts(items => items.filter(item => item.id !== id)); timers.current.delete(timer); }, error ? 10000 : 6000);
    timers.current.add(timer);
  }, []);
  const logout = useCallback(() => {
    clearSession(); setSession(null); setUser(null); setData(emptyData); setSelectedGame(null); setAuthMode(null); setError(null); setLoading(false); setMobileMenu(false);
  }, []);
  const navigate = useCallback(value => {
    setPage(value); setSelectedGame(null); setMobileMenu(false); window.location.hash = value; window.scrollTo({ top: 0, behavior: 'instant' });
  }, []);
  const refresh = useCallback(async (signal) => {
    const token = getSession()?.accessToken;
    const profile = await api('/me', { signal });
    const paths = ['/games', '/categories', '/developers', '/achievements', '/me/library', '/me/achievements', ...(profile.role === 'ADMIN' ? ['/users'] : [])];
    const responses = await Promise.all(paths.map(path => allPages(path, { signal })));
    if (getSession()?.accessToken !== token) return;
    const [games, categories, developers, achievements, library, unlocked, users] = responses;
    setUser(profile);
    setData({ games: games.items, categories: categories.items, developers: developers.items, achievements: achievements.items, library: library.items, unlocked: unlocked.items, users: users?.items || [] });
    setSelectedGame(previous => previous ? games.items.find(game => game.id === previous.id) || null : null);
    setError(null);
  }, []);
  useEffect(() => {
    if (!session) return;
    const controller = new AbortController();
    setLoading(true); setError(null);
    refresh(controller.signal).catch(e => { if (e.name !== 'AbortError') setError(e); }).finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [session, refresh]);
  useEffect(() => {
    const expired = () => { logout(); notify('Sua sessão expirou. Entre novamente para continuar.', true); };
    window.addEventListener('gb:session-expired', expired);
    const duration = session ? Math.max(0, Date.parse(session.expiresAt) - Date.now()) : null;
    const timer = duration !== null && duration < 2147483647 ? setTimeout(expired, duration) : null;
    return () => { window.removeEventListener('gb:session-expired', expired); clearTimeout(timer); };
  }, [session, logout, notify]);
  useEffect(() => {
    const change = () => { setPage(initialPage()); setMobileMenu(false); };
    window.addEventListener('hashchange', change);
    return () => window.removeEventListener('hashchange', change);
  }, []);
  useEffect(() => () => timers.current.forEach(clearTimeout), []);
  useEffect(() => { document.title = `${page === 'store' ? 'Descobrir' : page === 'library' ? 'Minha biblioteca' : page === 'achievements' ? 'Conquistas' : page === 'admin' ? 'Administração' : 'Meu perfil'} · GamersBusiness`; }, [page]);
  async function retry() { setLoading(true); setError(null); try { await refresh(); } catch (e) { setError(e); } finally { setLoading(false); } }
  const authenticated = Boolean(session);
  const effectivePage = page === 'admin' && user?.role !== 'ADMIN' ? 'store' : page;
  return <><a className="skip-link" href="#main-content">Pular para o conteúdo</a><header className="site-header"><div className="header-inner"><button className="brand" aria-label="GamersBusiness, início" onClick={() => navigate('store')}><span className="brand-symbol"><Gamepad2 size={28} strokeWidth={2.3} /></span><span>GAMERS<span>BUSINESS</span><small>PLAY YOUR WAY</small></span></button><nav className="header-nav" aria-label="Navegação principal"><button className={effectivePage === 'store' ? 'active' : ''} onClick={() => authenticated ? navigate('store') : setAuthMode('login')}>Loja</button><button className={effectivePage === 'library' ? 'active' : ''} onClick={() => authenticated ? navigate('library') : setAuthMode('login')}>Biblioteca</button><button className={effectivePage === 'achievements' ? 'active' : ''} onClick={() => authenticated ? navigate('achievements') : setAuthMode('login')}>Conquistas</button></nav><div className="header-actions">{authenticated ? <><button className="header-user" onClick={() => navigate('profile')} aria-label="Abrir meu perfil"><span className="mini-avatar">{user?.username?.slice(0, 1).toUpperCase() || 'G'}</span><span>{user?.username || 'Minha conta'}</span></button><button className="icon-button logout-button" aria-label="Sair da conta" onClick={logout}><LogOut size={19} /></button><button className="icon-button mobile-menu-toggle" aria-label={mobileMenu ? 'Fechar navegação' : 'Abrir navegação'} aria-expanded={mobileMenu} aria-controls="side-navigation" onClick={() => setMobileMenu(value => !value)}>{mobileMenu ? <X size={23} /> : <Menu size={23} />}</button></> : <><button className="header-login" onClick={() => setAuthMode('login')}><LogIn size={17} />Entrar</button><Button className="header-register" variant="lime" onClick={() => setAuthMode('register')}>Criar conta<ArrowUpRight size={16} /></Button></>}</div></div></header>
    {authenticated ? <div className="app-layout"><aside className={`sidebar ${mobileMenu ? 'sidebar-open' : ''}`} id="side-navigation"><div className="sidebar-title">SEU UNIVERSO</div><nav aria-label="Menu da conta">{navigation.map(item => <button key={item.id} className={`side-link ${effectivePage === item.id ? 'active' : ''}`} onClick={() => navigate(item.id)} aria-current={effectivePage === item.id ? 'page' : undefined}><item.icon size={19} /><span>{item.label}</span>{item.id === 'library' && <small>{data.library.length}</small>}</button>)}{user?.role === 'ADMIN' && <><div className="sidebar-divider" /><button className={`side-link ${effectivePage === 'admin' ? 'active' : ''}`} onClick={() => navigate('admin')}><ShieldCheck size={19} /><span>Administração</span></button></>}</nav><div className="sidebar-promo"><Gamepad2 size={29} /><strong>Jogue novas histórias.</strong><p>O seu próximo universo está a um clique.</p><button className="text-button" onClick={() => navigate('store')}>Explorar a loja<ArrowUpRight size={15} /></button></div><div className="sidebar-bottom"><span className="status-dot" />PROJETO ACADÊMICO</div></aside>
      {mobileMenu && <button className="mobile-backdrop" aria-label="Fechar navegação" onClick={() => setMobileMenu(false)} />}
      <main id="main-content" className="main-content" tabIndex={-1}>{loading ? <Loading /> : error ? <ErrorBox error={error} retry={retry} /> : user && <>{effectivePage === 'store' && <Store data={data} openGame={setSelectedGame} />}{effectivePage === 'library' && <Library data={data} openGame={setSelectedGame} refresh={refresh} notify={notify} goStore={() => navigate('store')} />}{effectivePage === 'achievements' && <Achievements data={data} openGame={setSelectedGame} goLibrary={() => navigate('library')} />}{effectivePage === 'profile' && <Profile user={user} onUpdate={setUser} notify={notify} />}{effectivePage === 'admin' && user.role === 'ADMIN' && <Admin data={data} user={user} refresh={refresh} notify={notify} onLogout={logout} />}</>}</main></div> : <main id="main-content" tabIndex={-1}><Welcome onLogin={() => setAuthMode('login')} onRegister={() => setAuthMode('register')} /></main>}
    <footer className={`site-footer ${authenticated ? 'footer-auth' : ''}`}><div><span className="footer-brand"><Gamepad2 size={21} />GAMERSBUSINESS</span><p>Um projeto acadêmico feito para quem ama jogos.</p></div><p>Sem vendas reais ou distribuição de jogos.<br />Artes dos jogos pertencem aos respectivos titulares.</p></footer>
    {authMode && !authenticated && <AuthModal initialMode={authMode} onClose={() => setAuthMode(null)} onAuthenticated={value => { setAuthMode(null); setSession(value); navigate('store'); }} />}
    {selectedGame && user && <GameDetail key={selectedGame.id} game={selectedGame} data={data} user={user} onClose={() => setSelectedGame(null)} refresh={refresh} notify={notify} />}
    <Toasts toasts={toasts} dismiss={id => setToasts(items => items.filter(item => item.id !== id))} />
  </>;
}
