import { useState } from 'react';
import { ArrowRight, ShieldCheck, Gamepad2 } from 'lucide-react';
import { api, saveSession } from '../api';
import { Modal, Field, Button, ErrorBox, Artwork } from './UI';

export function AuthModal({ initialMode = 'login', onClose, onAuthenticated }) {
  const [mode, setMode] = useState(initialMode);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const [created, setCreated] = useState(false);
  async function submit(event) {
    event.preventDefault(); setError(null); setBusy(true);
    const data = Object.fromEntries(new FormData(event.currentTarget));
    try {
      if (mode === 'register') {
        await api('/auth/register', { method: 'POST', anonymous: true, body: { username: data.username.trim(), email: data.email.trim(), password: data.password } });
        setCreated(true);
      }
      const session = await api('/auth/login', { method: 'POST', anonymous: true, body: { email: data.email.trim(), password: data.password } });
      saveSession(session);
      onAuthenticated(session);
    } catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <Modal title={mode === 'login' ? 'Bom ter você de volta.' : 'Seu próximo universo começa aqui.'} onClose={() => !busy && onClose()}>
    <div className="auth-body"><div className="auth-brand"><Gamepad2 size={25} /><span>UMA CONTA. MUITAS HISTÓRIAS.</span></div>
      <div className="tabs auth-tabs"><button disabled={busy} className={mode === 'login' ? 'active' : ''} onClick={() => { setMode('login'); setError(null); }}>Entrar</button><button disabled={busy} className={mode === 'register' ? 'active' : ''} onClick={() => { setMode('register'); setError(null); }}>Criar conta</button></div>
      {created && <p className="success-note">Conta criada. Se o acesso falhou, entre com os dados que você acabou de cadastrar.</p>}
      <form onSubmit={submit} className="form-stack"><fieldset className="form-controls" disabled={busy}>{mode === 'register' && <Field label="Nome de usuário" name="username" required maxLength={50} autoComplete="username" placeholder="Como quer ser chamado?" />}
      <Field label="E-mail" name="email" type="email" required maxLength={254} autoComplete="email" placeholder="voce@exemplo.com" />
      <Field label="Senha" name="password" type="password" required autoComplete={mode === 'login' ? 'current-password' : 'new-password'} placeholder="Sua senha" />
      <ErrorBox error={error} />
      <Button type="submit" busy={busy} className="full-width">{mode === 'login' ? 'Entrar na minha conta' : 'Criar conta e entrar'}<ArrowRight size={18} /></Button></fieldset></form><p className="auth-note"><ShieldCheck size={15} />Sua biblioteca fica vinculada à sua conta.</p>
    </div></Modal>;
}

export function Welcome({ onLogin, onRegister }) {
  return <div className="welcome">
    <section className="welcome-hero"><div className="welcome-visual"><Artwork title="Cyberpunk 2077" eager /></div><div className="welcome-shade" /><div className="welcome-copy"><span className="eyebrow">DESLIGUE DO ÓBVIO. ENTRE NO JOGO.</span><h1>Seu próximo<br />universo <em>te espera.</em></h1><p>Grandes histórias. Novos desafios. Um lugar para descobrir jogos e chamar de sua a próxima aventura.</p><div className="welcome-actions"><Button variant="lime" onClick={onRegister}>Comece sua jornada <ArrowRight size={20} /></Button><Button variant="glass" onClick={onLogin}>Já tenho uma conta</Button></div><span className="welcome-caption">Arte de Cyberpunk 2077 · Imagem ilustrativa</span></div><div className="hero-wordmark" aria-hidden="true">PLAY YOUR WAY.</div></section>
    <section className="welcome-editorial"><div><span className="eyebrow">SEU MUNDO, DO SEU JEITO</span><h2>Mais do que jogar.<br /><span>Uma coleção de experiências.</span></h2></div><p>Entre para explorar o catálogo completo, organizar sua biblioteca, registrar suas horas e compartilhar o que achou de cada aventura.</p></section>
    <div className="welcome-art-grid">{['ELDEN RING', 'Red Dead Redemption 2', 'Hollow Knight'].map((title, i) => <button onClick={onLogin} className="welcome-art-card" key={title}><Artwork title={title} /><span><small>EXPLORE NOVOS UNIVERSOS · 0{i + 1}</small><strong>{title}</strong><ArrowRight size={21} /></span></button>)}</div>
    <div className="welcome-disclaimer"><ShieldCheck size={18} /><p>Projeto acadêmico. Aquisições são simuladas, sem cobrança ou download de jogos. Entre para consultar os títulos e preços reais do catálogo.</p></div>
  </div>;
}
