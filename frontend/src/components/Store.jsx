import { useEffect, useMemo, useState } from 'react';
import { Search, ArrowUpRight, ChevronLeft, ChevronRight, SlidersHorizontal, Check, Gamepad2 } from 'lucide-react';
import { money, compactDate } from '../catalog';
import { Artwork, Button, Empty } from './UI';

export function Store({ data, openGame }) {
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [sort, setSort] = useState('title');
  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);
  const highlights = useMemo(() => data.games.slice(0, 5), [data.games]);
  const selected = highlights[index % (highlights.length || 1)];
  useEffect(() => {
    if (paused || highlights.length < 2 || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    const timer = setInterval(() => setIndex(value => (value + 1) % highlights.length), 8000);
    return () => clearInterval(timer);
  }, [highlights.length, paused]);
  const filtered = useMemo(() => data.games.filter(game => game.title.toLocaleLowerCase('pt-BR').includes(query.toLocaleLowerCase('pt-BR')) && (!category || game.categoryIds.includes(Number(category)))).sort((a, b) => sort === 'price-up' ? a.price - b.price : sort === 'price-down' ? b.price - a.price : sort === 'new' ? b.releaseDate.localeCompare(a.releaseDate) : a.title.localeCompare(b.title, 'pt-BR')), [data.games, query, category, sort]);
  const owned = new Set(data.library.map(entry => entry.gameId));
  return <>
    <div className="store-topline"><span className="eyebrow">EXPLORE. DESCUBRA. JOGUE.</span><span className="muted small">Seu próximo jogo favorito está aqui.</span></div>
    {selected && <section className="store-hero" onMouseEnter={() => setPaused(true)} onMouseLeave={() => setPaused(false)} onFocusCapture={() => setPaused(true)} onBlurCapture={event => { if (!event.currentTarget.contains(event.relatedTarget)) setPaused(false); }} aria-label="Destaques do catálogo">
      <div className="store-hero-image" key={selected.id}><Artwork title={selected.title} eager /></div><div className="store-hero-shade" />
      <div className="store-hero-copy"><span className="hero-label"><span />EM DESTAQUE</span><h1>{selected.title}</h1><p>{selected.description}</p><div className="hero-categories">{selected.categoryIds.map(id => <span key={id}>{data.categories.find(item => item.id === id)?.name || 'Jogo'}</span>)}</div><div className="hero-bottom"><div><small>{owned.has(selected.id) ? 'NA SUA BIBLIOTECA' : 'DISPONÍVEL NO CATÁLOGO'}</small><strong>{money(selected.price)}</strong></div><Button variant="lime" onClick={() => openGame(selected)}>Explorar jogo<ArrowUpRight size={19} /></Button></div></div>
      <div className="carousel-controls"><button className="icon-button" aria-label="Destaque anterior" onClick={() => setIndex(value => (value - 1 + highlights.length) % highlights.length)}><ChevronLeft size={19} /></button><div className="carousel-dots">{highlights.map((game, n) => <button key={game.id} onClick={() => setIndex(n)} className={selected.id === game.id ? 'active' : ''} aria-label={`Ver destaque ${game.title}`} aria-pressed={selected.id === game.id} />)}</div><button className="icon-button" aria-label="Próximo destaque" onClick={() => setIndex(value => (value + 1) % highlights.length)}><ChevronRight size={19} /></button></div>
    </section>}
    <section className="catalog-section"><div className="catalog-title"><div><span className="eyebrow">ENCONTRE SUA PRÓXIMA AVENTURA</span><h2>Explore o catálogo<span className="count-pill">{data.games.length}</span></h2></div><div className="search-field"><Search size={18} /><input aria-label="Buscar jogo pelo título" placeholder="Buscar um jogo…" value={query} onChange={event => setQuery(event.target.value)} />{query && <button aria-label="Limpar busca" className="clear-search" onClick={() => setQuery('')}>×</button>}</div></div>
      <div className="catalog-tools"><div className="chips" aria-label="Filtrar por categoria"><button className={`chip ${!category ? 'selected' : ''}`} onClick={() => setCategory('')}>Todos os jogos</button>{data.categories.map(item => <button className={`chip ${category === String(item.id) ? 'selected' : ''}`} aria-pressed={category === String(item.id)} onClick={() => setCategory(String(item.id))} key={item.id}>{item.name}</button>)}</div><label className="sort-select"><SlidersHorizontal size={16} /><span className="sr-only">Ordenação</span><select value={sort} onChange={event => setSort(event.target.value)}><option value="title">Nome: A–Z</option><option value="new">Lançamentos recentes</option><option value="price-up">Menor preço</option><option value="price-down">Maior preço</option></select></label></div>
      <p className="results-label">{filtered.length} {filtered.length === 1 ? 'jogo encontrado' : 'jogos encontrados'}{query && ` para “${query}”`}</p>
      {filtered.length ? <div className="game-grid">{filtered.map(game => <GameCard key={game.id} game={game} categories={data.categories} owned={owned.has(game.id)} onClick={() => openGame(game)} />)}</div> : <Empty title={data.games.length ? 'Nenhum jogo por aqui.' : 'O catálogo está começando.'}>{data.games.length ? 'Tente outro nome ou escolha uma categoria diferente.' : 'Os jogos cadastrados pela administração aparecerão aqui.'}{data.games.length > 0 && <button className="text-button" onClick={() => { setQuery(''); setCategory(''); }}>Limpar filtros</button>}</Empty>}
    </section><div className="store-footnote"><Gamepad2 size={23} /><span>A próxima boa história começa com um novo jogo.</span><small>Aquisições simuladas · Sem cobrança</small></div>
  </>;
}
export function GameCard({ game, categories, owned, onClick }) {
  return <button className="game-card" onClick={onClick}><div className="game-card-image"><Artwork title={game.title} />{owned && <span className="owned-badge"><Check size={13} />Na biblioteca</span>}<span className="game-card-arrow"><ArrowUpRight size={22} /></span></div><div className="game-card-body"><div className="game-card-meta">{categories.find(item => game.categoryIds.includes(item.id))?.name || 'Jogo'}<span>{compactDate(game.releaseDate)}</span></div><h3>{game.title}</h3><div className="game-card-bottom"><span>Preço do catálogo</span><strong>{money(game.price)}</strong></div></div></button>;
}
