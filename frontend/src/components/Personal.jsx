import { useMemo, useState } from 'react';
import { Clock3, Trash2, Pencil, Trophy, LibraryBig, ArrowUpRight, ShieldCheck, Search, Check, UserRound } from 'lucide-react';
import { api } from '../api';
import { date, money } from '../catalog';
import { Artwork, Button, Confirm, Empty, ErrorBox, Field, Modal, SectionHeading } from './UI';

export function Library({ data, openGame, refresh, notify, goStore }) {
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState(null);
  const [removing, setRemoving] = useState(null);
  const entries = data.library.map(entry => ({ ...entry, game: data.games.find(game => game.id === entry.gameId) })).filter(entry => !query || (entry.game?.title || '').toLocaleLowerCase('pt-BR').includes(query.toLocaleLowerCase('pt-BR')));
  return <><SectionHeading eyebrow="SUA COLEÇÃO, SUAS HISTÓRIAS" title="Minha biblioteca"><span className="count-pill">{data.library.length} jogos</span></SectionHeading>
    <div className="personal-stats"><div><LibraryBig size={22} /><strong>{data.library.length}</strong><span>Jogos na coleção</span></div><div><Clock3 size={22} /><strong>{data.library.reduce((total, item) => total + item.hoursPlayed, 0)} h</strong><span>Horas registradas</span></div><div><Trophy size={22} /><strong>{data.unlocked.length}</strong><span>Conquistas desbloqueadas</span></div></div>
    {data.library.length > 0 && <div className="library-toolbar"><h2>Todos os seus jogos</h2><div className="search-field"><Search size={18} /><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Buscar na biblioteca…" aria-label="Buscar na biblioteca" /></div></div>}
    {entries.length ? <div className="library-grid">{entries.map(entry => <article className="library-card" key={entry.id}><button className="library-image" disabled={!entry.game} onClick={() => openGame(entry.game)} aria-label={`Ver ${entry.game?.title || 'jogo'}`}><Artwork title={entry.game?.title || `Jogo #${entry.gameId}`} /><span><ArrowUpRight size={22} /></span></button><div className="library-card-body"><h3>{entry.game?.title || `Jogo #${entry.gameId}`}</h3><p><Clock3 size={15} />{entry.hoursPlayed} horas registradas</p><div className="library-info"><span>Adquirido em {date(entry.acquiredAt)}</span><span>Valor registrado: {money(entry.paidPrice)}</span></div><div className="library-actions"><Button variant="secondary" onClick={() => setEditing(entry)}><Pencil size={15} />Editar horas</Button><button className="icon-button danger-text" aria-label={`Remover ${entry.game?.title || 'jogo'} da biblioteca`} onClick={() => setRemoving(entry)}><Trash2 size={18} /></button></div></div></article>)}</div> : <Empty icon={LibraryBig} title={query ? 'Nenhum jogo encontrado.' : 'Sua coleção começa aqui.'} action={!query && <Button onClick={goStore}>Explorar a loja<ArrowUpRight size={17} /></Button>}>{query ? 'Tente buscar por outro título.' : 'Encontre seu próximo jogo favorito e adicione-o à biblioteca, sem cobrança.'}</Empty>}
    {editing && <HoursModal entry={editing} onClose={() => setEditing(null)} onSaved={async () => { await refresh(); notify('Horas de jogo atualizadas.'); setEditing(null); }} />}
    {removing && <Confirm title="Remover jogo da biblioteca?" danger label="Remover jogo" onClose={() => setRemoving(null)} onConfirm={async () => { await api(`/me/library/${removing.id}`, { method: 'DELETE' }); await refresh(); notify('Jogo removido da biblioteca.'); }}>Você vai remover <strong>{removing.game?.title || 'este jogo'}</strong> da coleção. As horas desta aquisição serão perdidas. Você poderá adquiri-lo novamente, mas esta ação não pode ser desfeita.</Confirm>}
  </>;
}
function HoursModal({ entry, onClose, onSaved }) {
  const [hours, setHours] = useState(entry.hoursPlayed);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError(null);
    try { await api(`/me/library/${entry.id}`, { method: 'PATCH', body: { hoursPlayed: Number(hours) } }); await onSaved(); }
    catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <Modal title="Seu tempo de jogo" onClose={() => !busy && onClose()}><form className="modal-body form-stack" onSubmit={submit}><fieldset className="form-controls" disabled={busy}><p className="muted">{entry.game?.title || `Jogo #${entry.gameId}`} · Registro manual de horas.</p><Field label="Total de horas jogadas" name="hours" type="number" min="0" max="2147483647" step="1" required value={hours} onChange={event => setHours(event.target.value)} /><ErrorBox error={error} /><div className="form-actions"><Button variant="secondary" onClick={onClose} type="button" disabled={busy}>Cancelar</Button><Button type="submit" busy={busy}>Salvar horas</Button></div></fieldset></form></Modal>;
}

export function Achievements({ data, openGame, goLibrary }) {
  const [filter, setFilter] = useState('all');
  const unlocked = new Map(data.unlocked.map(item => [item.achievementId, item]));
  const owned = new Set(data.library.map(item => item.gameId));
  const available = useMemo(() => data.achievements.filter(item => owned.has(item.gameId) || unlocked.has(item.id)), [data.achievements, data.library, data.unlocked]);
  const list = available.filter(item => filter === 'all' || (filter === 'unlocked' ? unlocked.has(item.id) : !unlocked.has(item.id)));
  return <><SectionHeading eyebrow="CADA DESAFIO CONTA" title="Minhas conquistas"><span className="count-pill">{data.unlocked.length} desbloqueadas</span></SectionHeading><div className="achievement-intro"><div className="achievement-intro-icon"><Trophy size={40} /></div><div><h2>Pequenas vitórias. Grandes histórias.</h2><p>Suas conquistas e os próximos desafios dos jogos da sua biblioteca, em um só lugar. Desbloqueie-as na página de cada jogo.</p></div></div><div className="chips achievement-filters">{[['all', 'Todas'], ['unlocked', 'Desbloqueadas'], ['locked', 'A conquistar']].map(([value, label]) => <button className={`chip ${filter === value ? 'selected' : ''}`} key={value} onClick={() => setFilter(value)} aria-pressed={filter === value}>{label}</button>)}</div>
    {list.length ? <div className="achievement-grid">{list.map(item => { const game = data.games.find(game => game.id === item.gameId); const record = unlocked.get(item.id); return <article className={`achievement-card ${record ? 'is-unlocked' : ''}`} key={item.id}><div className="achievement-symbol"><Trophy size={27} /></div><div className="achievement-card-copy"><span className="eyebrow">{game?.title || `Jogo #${item.gameId}`}</span><h3>{item.name}</h3><p>{item.description}</p>{record ? <span className="achievement-status"><Check size={14} />{date(record.unlockedAt)}</span> : <span className="muted small">Ainda não desbloqueada</span>}</div>{game && <button className="icon-button" onClick={() => openGame(game)} aria-label={`Abrir ${game.title}`}><ArrowUpRight size={21} /></button>}</article>; })}</div> : <Empty icon={Trophy} title="Uma nova conquista está por vir." action={<Button variant="secondary" onClick={goLibrary}>Ver minha biblioteca</Button>}>{available.length ? 'Nenhuma conquista com este filtro.' : 'Adicione jogos com conquistas cadastradas à sua biblioteca para começar.'}</Empty>}
  </>;
}

export function Profile({ user, onUpdate, notify }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  async function submit(event) {
    event.preventDefault(); setError(null);
    const form = event.currentTarget;
    const fields = Object.fromEntries(new FormData(form));
    if ((fields.newPassword || fields.currentPassword) && (!fields.newPassword || !fields.currentPassword)) { setError(new Error('Informe a senha atual e a nova senha para alterar sua senha.')); return; }
    if (fields.newPassword && fields.newPassword !== fields.confirmPassword) { setError(new Error('A confirmação da nova senha não confere.')); return; }
    if (new TextEncoder().encode(fields.newPassword || '').length > 72) { setError(new Error('A nova senha deve ter no máximo 72 bytes UTF-8.')); return; }
    const body = {};
    if (fields.username.trim() !== user.username) body.username = fields.username.trim();
    if (fields.email.trim() !== user.email) body.email = fields.email.trim();
    if (fields.newPassword) { body.currentPassword = fields.currentPassword; body.newPassword = fields.newPassword; }
    if (!Object.keys(body).length) { notify('Nenhuma alteração para salvar.'); return; }
    setBusy(true);
    try {
      const profile = await api('/me', { method: 'PATCH', body });
      onUpdate(profile);
      form.elements.currentPassword.value = ''; form.elements.newPassword.value = ''; form.elements.confirmPassword.value = '';
      notify('Perfil atualizado com sucesso.');
    } catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <><SectionHeading eyebrow="SEU ESPAÇO NO GAMERSBUSINESS" title="Meu perfil" /><div className="profile-layout"><aside className="profile-card"><div className="profile-avatar">{user.username.slice(0, 2).toUpperCase()}</div><h2>{user.username}</h2><p>{user.email}</p><span className="role-badge">{user.role === 'ADMIN' ? 'Administrador' : 'Jogador'}</span><div className="profile-since"><ShieldCheck size={18} /><span>Membro desde<br /><strong>{date(user.registeredAt)}</strong></span></div></aside><form className="profile-form" onSubmit={submit}><fieldset className="form-controls" disabled={busy}><div className="subsection-heading"><h2><UserRound size={20} />Informações da conta</h2></div><div className="form-grid"><Field label="Nome de usuário" name="username" defaultValue={user.username} required maxLength={50} autoComplete="username" /><Field label="E-mail" name="email" type="email" defaultValue={user.email} required maxLength={254} autoComplete="email" /></div><div className="profile-password-heading"><h3>Alterar senha</h3><p>Deixe os campos abaixo vazios para manter sua senha atual.</p></div><Field label="Senha atual" name="currentPassword" type="password" autoComplete="current-password" /><div className="form-grid"><Field label="Nova senha" name="newPassword" type="password" autoComplete="new-password" hint="Até 72 bytes UTF-8." /><Field label="Confirme a nova senha" name="confirmPassword" type="password" autoComplete="new-password" /></div><ErrorBox error={error} /><div className="form-actions"><Button type="submit" busy={busy}>Salvar alterações</Button></div></fieldset></form></div></>;
}
