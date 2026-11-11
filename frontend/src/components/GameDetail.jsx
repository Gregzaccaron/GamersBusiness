import { useCallback, useEffect, useState } from 'react';
import { ArrowRight, Check, Clock3, Star, Trophy, MessageSquare, Trash2, CalendarDays, Building2 } from 'lucide-react';
import { allPages, api } from '../api';
import { date, money } from '../catalog';
import { Artwork, Button, Confirm, Empty, ErrorBox, Field, Loading, Modal } from './UI';

export function GameDetail({ game, data, user, onClose, refresh, notify }) {
  const [reviews, setReviews] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [confirmAcquire, setConfirmAcquire] = useState(false);
  const [deleteReview, setDeleteReview] = useState(null);
  const [unlocking, setUnlocking] = useState(null);
  const entry = data.library.find(item => item.gameId === game.id);
  const achievements = data.achievements.filter(item => item.gameId === game.id);
  const unlocked = new Set(data.unlocked.map(item => item.achievementId));
  const ownReview = reviews?.items.find(item => item.userId === user.id);
  const loadReviews = useCallback(async signal => {
    setLoading(true); setError(null);
    try { setReviews(await allPages(`/games/${game.id}/reviews`, { signal })); }
    catch (e) { if (e.name !== 'AbortError') setError(e); }
    finally { if (!signal?.aborted) setLoading(false); }
  }, [game.id]);
  useEffect(() => { const controller = new AbortController(); loadReviews(controller.signal); return () => controller.abort(); }, [loadReviews]);
  async function acquire() {
    await api('/me/library', { method: 'POST', body: { gameId: game.id } });
    await refresh(); notify('Jogo adicionado à sua biblioteca. Nenhuma cobrança foi realizada.');
  }
  async function unlock(achievement) {
    setUnlocking(achievement.id);
    try { await api('/me/achievements', { method: 'POST', body: { achievementId: achievement.id } }); await refresh(); notify(`Conquista desbloqueada: ${achievement.name}`); }
    catch (e) { notify(e.message, true); } finally { setUnlocking(null); }
  }
  return <>
    <Modal wide title={game.title} onClose={onClose} className="detail-modal"><div className="detail-banner"><Artwork title={game.title} eager /><div className="detail-banner-shade" /><span className="detail-kicker">DESCUBRA SUA PRÓXIMA HISTÓRIA</span></div>
      <div className="detail-content"><div className="detail-main"><div className="detail-tags">{game.categoryIds.map(id => <span className="tag" key={id}>{data.categories.find(item => item.id === id)?.name || 'Categoria removida'}</span>)}</div><h2>Sobre o jogo</h2><p className="game-description">{game.description}</p><div className="game-facts"><div><Building2 size={18} /><span>Desenvolvedora<strong>{data.developers.find(item => item.id === game.developerId)?.name || 'Não informada'}</strong></span></div><div><CalendarDays size={18} /><span>Lançamento<strong>{date(game.releaseDate)}</strong></span></div></div></div>
      <aside className="purchase-panel"><span className="eyebrow">{entry ? 'ESTE JOGO É SEU' : 'ADICIONE À SUA COLEÇÃO'}</span><strong className="detail-price">{money(game.price)}</strong>{entry ? <><span className="in-library"><Check size={17} />Na sua biblioteca</span><p><Clock3 size={16} />{entry.hoursPlayed} horas registradas</p><small>Adquirido em {date(entry.acquiredAt)}</small></> : <><Button variant="lime" onClick={() => setConfirmAcquire(true)}>Adicionar à biblioteca<ArrowRight size={18} /></Button><small>Aquisição simulada. Sem pagamento, licença comercial ou download.</small></>}</aside>
      <section className="detail-section"><div className="subsection-heading"><h2><Trophy size={21} />Conquistas</h2><span>{achievements.filter(item => unlocked.has(item.id)).length} / {achievements.length}</span></div>{achievements.length ? <div className="achievement-list">{achievements.map(item => <div className={`achievement-row ${unlocked.has(item.id) ? 'is-unlocked' : ''}`} key={item.id}><div className="achievement-symbol"><Trophy size={23} /></div><div><strong>{item.name}</strong><p>{item.description}</p></div>{unlocked.has(item.id) ? <span className="achievement-status"><Check size={15} />Desbloqueada</span> : entry ? <Button variant="secondary" busy={unlocking === item.id} disabled={unlocking !== null} onClick={() => unlock(item)}>Desbloquear</Button> : <span className="muted small">Requer o jogo na biblioteca</span>}</div>)}</div> : <p className="muted">Ainda não há conquistas cadastradas para este jogo.</p>}{entry && achievements.length > 0 && <p className="small muted">Neste projeto acadêmico, você registra manualmente suas conquistas.</p>}</section>
      <section className="detail-section reviews-section"><div className="subsection-heading"><h2><MessageSquare size={21} />Avaliações da comunidade</h2>{reviews && reviews.reviewCount > 0 && <span className="rating-summary"><Star size={17} fill="currentColor" />{Number(reviews.averageRating).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} / 5 <small>({reviews.reviewCount})</small></span>}</div>
        {loading ? <Loading text="Carregando avaliações…" /> : error ? <ErrorBox error={error} retry={() => loadReviews()} /> : <>
          {entry && <ReviewForm key={ownReview?.id || 'new'} gameId={game.id} review={ownReview} onSaved={async () => { await loadReviews(); notify(ownReview ? 'Avaliação atualizada.' : 'Avaliação publicada.'); }} />}
          {!entry && <p className="review-requirement">Adicione este jogo à sua biblioteca para publicar ou editar uma avaliação.</p>}
          {reviews?.items.length ? <div className="reviews-list">{reviews.items.map(review => <article className="review" key={review.id}><div className="review-avatar">{review.userId === user.id ? user.username.slice(0, 1).toUpperCase() : 'J'}</div><div className="review-content"><div className="review-top"><strong>{review.userId === user.id ? `${user.username} (você)` : `Jogador #${review.userId}`}</strong><span className="stars" aria-label={`${review.rating} de 5 estrelas`}>{'★'.repeat(review.rating)}<span>{'★'.repeat(5 - review.rating)}</span></span></div><time>{date(review.reviewedAt)}</time><p>{review.comment || 'Sem comentário.'}</p>{review.userId === user.id && <button className="text-button danger-text" onClick={() => setDeleteReview(review)}><Trash2 size={14} />Excluir avaliação</button>}</div></article>)}</div> : <Empty icon={MessageSquare} title="A conversa começa com você.">Este jogo ainda não recebeu avaliações.</Empty>}
        </>}
      </section></div>
    </Modal>
    {confirmAcquire && <Confirm title="Adicionar à biblioteca?" label="Confirmar aquisição simulada" onClose={() => setConfirmAcquire(false)} onConfirm={acquire}>Você está adicionando <strong>{game.title}</strong> por <strong>{money(game.price)}</strong>. Esta é uma simulação acadêmica: nenhum valor será cobrado e nenhum jogo será disponibilizado para download.</Confirm>}
    {deleteReview && <Confirm title="Excluir sua avaliação?" label="Excluir avaliação" danger onClose={() => setDeleteReview(null)} onConfirm={async () => { await api(`/reviews/${deleteReview.id}`, { method: 'DELETE' }); await loadReviews(); notify('Avaliação excluída.'); }}>Seu comentário e sua nota serão removidos. Esta ação não pode ser desfeita.</Confirm>}
  </>;
}

function ReviewForm({ gameId, review, onSaved }) {
  const [rating, setRating] = useState(review?.rating || 5);
  const [comment, setComment] = useState(review?.comment || '');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError(null);
    try { await api(review ? `/reviews/${review.id}` : `/games/${gameId}/reviews`, { method: review ? 'PATCH' : 'POST', body: { rating: Number(rating), comment: comment.trim() || null } }); await onSaved(); }
    catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <form className="review-form" onSubmit={submit}><fieldset className="form-controls" disabled={busy}><h3>{review ? 'Sua avaliação' : 'Qual foi a sua experiência?'}</h3><Field label="Sua nota" name="rating"><select id="rating" value={rating} onChange={event => setRating(Number(event.target.value))}>{[5, 4, 3, 2, 1].map(value => <option key={value} value={value}>{value} {value === 1 ? 'estrela' : 'estrelas'}</option>)}</select></Field><Field label="Comentário (opcional)" name="comment"><textarea id="comment" rows={3} value={comment} onChange={event => setComment(event.target.value)} placeholder="Conte à comunidade o que você achou…" /></Field><ErrorBox error={error} /><Button type="submit" busy={busy} variant="secondary">{review ? 'Salvar avaliação' : 'Publicar avaliação'}</Button></fieldset></form>;
}
