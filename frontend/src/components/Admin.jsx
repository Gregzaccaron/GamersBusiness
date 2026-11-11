import { useState } from 'react';
import { Gamepad2, Tags, Building2, Trophy, Users, Plus, Pencil, Trash2, Search, ShieldCheck } from 'lucide-react';
import { api } from '../api';
import { money, date } from '../catalog';
import { Button, Confirm, Empty, ErrorBox, Field, Modal, SectionHeading } from './UI';

const resources = {
  games: { label: 'Jogos', singular: 'jogo', icon: Gamepad2 },
  categories: { label: 'Categorias', singular: 'categoria', icon: Tags },
  developers: { label: 'Desenvolvedoras', singular: 'desenvolvedora', icon: Building2 },
  achievements: { label: 'Conquistas', singular: 'conquista', icon: Trophy },
  users: { label: 'Usuários', singular: 'usuário', icon: Users },
};

export function Admin({ data, user, refresh, notify, onLogout }) {
  const [resource, setResource] = useState('games');
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState(null);
  const [removing, setRemoving] = useState(null);
  const config = resources[resource];
  const items = (data[resource] || []).filter(item => `${item.title || item.name || item.username} ${item.email || ''}`.toLocaleLowerCase('pt-BR').includes(query.toLocaleLowerCase('pt-BR')));
  async function remove() {
    await api(`/${resource}/${removing.id}`, { method: 'DELETE' });
    if (resource === 'users' && removing.id === user.id) { onLogout(); return; }
    await refresh(); notify(`${config.singular[0].toUpperCase()}${config.singular.slice(1)} removido(a).`);
  }
  return <><SectionHeading eyebrow="GESTÃO DO GAMERSBUSINESS" title="Administração"><span className="role-badge"><ShieldCheck size={14} />Acesso restrito</span></SectionHeading><div className="admin-tabs" role="tablist" aria-label="Recursos administrativos">{Object.entries(resources).map(([key, item]) => <button key={key} role="tab" aria-selected={resource === key} className={resource === key ? 'active' : ''} onClick={() => { setResource(key); setQuery(''); }}><item.icon size={18} />{item.label}<span>{data[key]?.length || 0}</span></button>)}</div>
    <section className="admin-panel"><div className="admin-toolbar"><div><h2>{config.label}</h2><p>Gerencie os registros persistidos no catálogo.</p></div>{resource !== 'users' && <Button onClick={() => setEditing({})}><Plus size={18} />Adicionar {config.singular}</Button>}</div><div className="search-field admin-search"><Search size={18} /><input value={query} onChange={event => setQuery(event.target.value)} aria-label={`Buscar ${config.label.toLowerCase()}`} placeholder={`Buscar ${config.label.toLowerCase()}…`} /></div>
      {items.length ? <div className="table-scroll"><table className="admin-table"><thead><tr><th scope="col">{resource === 'games' ? 'Jogo' : resource === 'users' ? 'Usuário' : 'Nome'}</th><th scope="col">Detalhes</th><th scope="col" className="actions-column">Ações</th></tr></thead><tbody>{items.map(item => <tr key={item.id}><td><strong>{item.title || item.name || item.username}</strong><small>#{item.id}</small></td><td><ResourceDetails resource={resource} item={item} data={data} /></td><td><div className="table-actions">{resource !== 'users' && <button className="icon-button" aria-label={`Editar ${item.title || item.name}`} onClick={() => setEditing(item)}><Pencil size={17} /></button>}<button className="icon-button danger-text" aria-label={`Excluir ${item.title || item.name || item.username}`} onClick={() => setRemoving(item)}><Trash2 size={17} /></button></div></td></tr>)}</tbody></table></div> : <Empty icon={config.icon} title={query ? 'Nenhum registro encontrado.' : `Nenhum registro de ${config.label.toLowerCase()}.`}>{query ? 'Tente outro termo de busca.' : 'Adicione o primeiro registro para começar.'}</Empty>}
    </section>
    {editing && <ResourceEditor key={`${resource}-${editing.id || 'new'}`} resource={resource} item={editing} data={data} onClose={() => setEditing(null)} onSaved={async () => { await refresh(); notify('Registro salvo com sucesso.'); setEditing(null); }} />}
    {removing && <Confirm title={`Excluir ${config.singular}?`} danger label="Excluir definitivamente" onClose={() => setRemoving(null)} onConfirm={remove}>Você está excluindo <strong>{removing.title || removing.name || removing.username}</strong>. Esta ação não pode ser desfeita. Registros vinculados podem impedir a exclusão.{resource === 'users' && removing.id === user.id && ' Esta é a sua conta: você será desconectado.'}</Confirm>}
  </>;
}
function ResourceDetails({ resource, item, data }) {
  if (resource === 'games') return <><span>{money(item.price)}</span><small>{data.developers.find(dev => dev.id === item.developerId)?.name || `Desenvolvedora #${item.developerId}`} · {date(item.releaseDate)}</small></>;
  if (resource === 'developers') return <><span>{item.country}</span><small>Fundação: {date(item.foundationDate)}</small></>;
  if (resource === 'achievements') return <><span>{data.games.find(game => game.id === item.gameId)?.title || `Jogo #${item.gameId}`}</span><small className="table-description">{item.description}</small></>;
  if (resource === 'users') return <><span>{item.email}</span><small>{item.role === 'ADMIN' ? 'Administrador' : 'Jogador'} · {date(item.registeredAt)}</small></>;
  return <span>{data.games.filter(game => game.categoryIds.includes(item.id)).length} jogos vinculados</span>;
}

function ResourceEditor({ resource, item, data, onClose, onSaved }) {
  const config = resources[resource];
  const [values, setValues] = useState({ title: '', name: '', description: '', price: '', releaseDate: '', developerId: '', categoryIds: [], country: '', foundationDate: '', gameId: '', ...item });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const update = key => event => setValues(previous => ({ ...previous, [key]: event.target.value }));
  function textField(key, label, options = {}) { return <Field key={key} label={label} name={key} value={values[key]} onChange={update(key)} required {...options} />; }
  function longField(key, label) { return <Field label={label} name={key}><textarea id={key} value={values[key]} onChange={update(key)} rows={4} required /></Field>; }
  async function submit(event) {
    event.preventDefault(); setError(null);
    let body;
    if (resource === 'games') {
      if (!values.categoryIds.length) { setError(new Error('Selecione pelo menos uma categoria para o jogo.')); return; }
      body = { title: values.title.trim(), description: values.description.trim(), price: Number(values.price), releaseDate: values.releaseDate, developerId: Number(values.developerId), categoryIds: values.categoryIds };
    } else if (resource === 'developers') body = { name: values.name.trim(), country: values.country.trim(), foundationDate: values.foundationDate };
    else if (resource === 'achievements') body = { gameId: Number(values.gameId), name: values.name.trim(), description: values.description.trim() };
    else body = { name: values.name.trim() };
    setBusy(true);
    try { await api(`/${resource}${item.id ? `/${item.id}` : ''}`, { method: item.id ? 'PUT' : 'POST', body }); await onSaved(); }
    catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <Modal title={`${item.id ? 'Editar' : 'Adicionar'} ${config.singular}`} onClose={() => !busy && onClose()} wide={resource === 'games'}><form className="modal-body form-stack" onSubmit={submit}><fieldset className="form-controls" disabled={busy}>{resource === 'games' && <>
    {textField('title', 'Título do jogo', { maxLength: 160 })}
    {longField('description', 'Descrição')}
    <div className="form-grid">{textField('price', 'Preço (R$)', { type: 'number', min: '0', max: '9999999999.99', step: '0.01' })}{textField('releaseDate', 'Data de lançamento', { type: 'date' })}</div>
    <Field label="Desenvolvedora" name="developerId"><select id="developerId" value={values.developerId} onChange={update('developerId')} required><option value="">Selecione uma desenvolvedora</option>{data.developers.map(dev => <option value={dev.id} key={dev.id}>{dev.name}</option>)}</select></Field>
    {!data.developers.length && <p className="small warning-note">Cadastre uma desenvolvedora antes de adicionar o jogo.</p>}
    <fieldset className="category-options"><legend>Categorias · selecione pelo menos uma</legend>{data.categories.map(category => <label key={category.id}><input type="checkbox" checked={values.categoryIds.includes(category.id)} onChange={event => setValues(previous => ({ ...previous, categoryIds: event.target.checked ? [...previous.categoryIds, category.id] : previous.categoryIds.filter(id => id !== category.id) }))} />{category.name}</label>)}</fieldset>
    {!data.categories.length && <p className="small warning-note">Cadastre uma categoria antes de adicionar o jogo.</p>}
    <p className="small muted">A arte local é associada pelo título exato aos jogos da seleção inicial. Outros títulos recebem uma capa neutra com seu nome.</p>
  </>}
  {resource === 'categories' && textField('name', 'Nome da categoria', { maxLength: 100 })}
  {resource === 'developers' && <>{textField('name', 'Nome da desenvolvedora', { maxLength: 120 })}<div className="form-grid">{textField('country', 'País', { maxLength: 100 })}{textField('foundationDate', 'Data de fundação', { type: 'date' })}</div></>}
  {resource === 'achievements' && <><Field label="Jogo" name="gameId"><select id="gameId" value={values.gameId} onChange={update('gameId')} required><option value="">Selecione um jogo</option>{data.games.map(game => <option key={game.id} value={game.id}>{game.title}</option>)}</select></Field>{!data.games.length && <p className="warning-note small">Cadastre um jogo antes de adicionar conquistas.</p>}{textField('name', 'Nome da conquista', { maxLength: 120 })}{longField('description', 'Descrição da conquista')}</>}
  <ErrorBox error={error} /><div className="form-actions"><Button variant="secondary" type="button" disabled={busy} onClick={onClose}>Cancelar</Button><Button type="submit" busy={busy} disabled={(resource === 'games' && (!data.categories.length || !data.developers.length)) || (resource === 'achievements' && !data.games.length)}>Salvar {config.singular}</Button></div></fieldset></form></Modal>;
}
