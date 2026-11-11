import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { X, AlertCircle, LoaderCircle, Gamepad2, ArrowRight, Check } from 'lucide-react';
import { imageFor } from '../catalog';

export function Button({ children, variant = 'primary', busy = false, className = '', ...props }) {
  return <button className={`button ${variant} ${className}`} {...props} disabled={props.disabled || busy}>{busy && <LoaderCircle size={17} className="spin" />}{children}</button>;
}
export function Field({ label, hint, children, ...props }) {
  const id = props.id || props.name;
  return <label className="field" htmlFor={id}><span>{label}</span>{children || <input id={id} {...props} />}{hint && <small>{hint}</small>}</label>;
}
export function ErrorBox({ error, retry }) {
  if (!error) return null;
  return <div className="error-box" role="alert"><AlertCircle size={19} /><div><strong>Não foi possível concluir</strong><p>{error.message || String(error)}</p>{retry && <button className="text-button" onClick={retry}>Tentar novamente <ArrowRight size={14} /></button>}</div></div>;
}
export function Loading({ text = 'Carregando seu universo…' }) {
  return <div className="loading" role="status"><LoaderCircle className="spin" size={27} /><span>{text}</span></div>;
}
export function Empty({ icon: Icon = Gamepad2, title, children, action }) {
  return <div className="empty-state"><div className="empty-icon"><Icon size={34} /></div><h3>{title}</h3>{children && <p>{children}</p>}{action}</div>;
}
export function Artwork({ title, className = '', eager = false, hero = eager }) {
  const src = imageFor(title, hero);
  const [failed, setFailed] = useState(false);
  return src && !failed ? <img className={`artwork ${className}`} src={src} alt={title} loading={eager ? 'eager' : 'lazy'} onError={() => setFailed(true)} /> : <div className={`artwork art-fallback ${className}`}><Gamepad2 size={48} /><span>{title}</span></div>;
}
export function Modal({ title, children, onClose, wide = false, className = '' }) {
  const ref = useRef(null);
  const closeRef = useRef(onClose);
  closeRef.current = onClose;
  useEffect(() => {
    const dialog = ref.current;
    const previous = document.activeElement;
    dialog.showModal();
    return () => {
      dialog.close();
      previous?.focus?.();
    };
  }, []);
  return <dialog ref={ref} className={`modal ${wide ? 'modal-wide' : ''} ${className}`} aria-label={title} onCancel={event => { event.preventDefault(); closeRef.current(); }} onClick={event => { if (event.target === ref.current) closeRef.current(); }}><div className="modal-shell"><div className="modal-heading"><h2>{title}</h2><button type="button" className="icon-button" aria-label="Fechar janela" onClick={onClose}><X size={22} /></button></div>{children}</div></dialog>;
}
export function Confirm({ title, children, onConfirm, onClose, label = 'Confirmar', danger = false }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  async function submit() {
    setBusy(true); setError(null);
    try { await onConfirm(); onClose(); } catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <Modal title={title} onClose={() => !busy && onClose()}><div className="modal-body"><p className="confirm-copy">{children}</p><ErrorBox error={error} /><div className="form-actions"><Button variant="secondary" disabled={busy} onClick={onClose}>Cancelar</Button><Button variant={danger ? 'danger' : 'lime'} busy={busy} onClick={submit}>{label}</Button></div></div></Modal>;
}
export function Toasts({ toasts, dismiss }) {
  const [host, setHost] = useState(document.body);
  useLayoutEffect(() => {
    const dialogs = document.querySelectorAll('dialog[open]');
    const next = dialogs.length ? dialogs[dialogs.length - 1] : document.body;
    if (next !== host) setHost(next);
  });
  return createPortal(<div className="toast-stack" aria-live="polite">{toasts.map(toast => <div className={`toast ${toast.error ? 'toast-error' : ''}`} key={toast.id}>{toast.error ? <AlertCircle size={19} /> : <Check size={19} />}<span>{toast.message}</span><button className="icon-button" onClick={() => dismiss(toast.id)} aria-label="Dispensar mensagem"><X size={16} /></button></div>)}</div>, host);
}
export function SectionHeading({ eyebrow, title, children }) {
  return <div className="section-heading"><div>{eyebrow && <span className="eyebrow">{eyebrow}</span>}<h1>{title}</h1></div>{children}</div>;
}
