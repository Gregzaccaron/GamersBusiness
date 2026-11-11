export const artwork = {
  'Cyberpunk 2077': '1091500',
  'Red Dead Redemption 2': '1174180',
  'ELDEN RING': '1245620',
  'Hollow Knight': '367520',
  'Stardew Valley': '413150',
  'Hades': '1145360',
  "Baldur's Gate 3": '1086940',
  'Forza Horizon 5': '1551360',
  'God of War': '1593500',
  'The Witcher 3: Wild Hunt': '292030',
  'Portal 2': '620',
  'Celeste': '504230',
  'Counter-Strike 2': '730',
  'Dota 2': '570',
  'Team Fortress 2': '440',
  'Apex Legends': '1172470',
  'Warframe': '230410',
  'Path of Exile': '238960',
  'Destiny 2': '1085660',
  'PUBG: BATTLEGROUNDS': '578080',
  "No Man's Sky": '275850',
  'Subnautica': '264710',
  'Terraria': '105600',
  'Valheim': '892970',
  'Satisfactory': '526870',
  'Factorio': '427520',
  'RimWorld': '294100',
  "Sid Meier's Civilization VI": '289070',
  'Age of Empires II: Definitive Edition': '813780',
  'Cities: Skylines': '255710',
  'Euro Truck Simulator 2': '227300',
  'Assetto Corsa': '244210',
  'DOOM Eternal': '782330',
  'Resident Evil 4': '2050650',
  'Dead Space': '1693980',
  'Sekiro: Shadows Die Twice': '814380',
  'DARK SOULS III': '374320',
  'Lies of P': '1627720',
  'Ori and the Will of the Wisps': '1057090',
  'Cuphead': '268910',
  'Dead Cells': '588650',
  'Slay the Spire': '646570',
  'Balatro': '2379780',
  'Disco Elysium': '632470',
  'Outer Wilds': '753640',
  'It Takes Two': '1426210',
  'Overcooked! 2': '728880',
  'Vampire Survivors': '1794680',
};
export function imageFor(title, hero = false) { return artwork[title] ? `/images/${artwork[title]}${hero ? '-hero' : ''}.jpg` : null; }
const currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const dateFormatter = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' });
export const money = value => currencyFormatter.format(value);
export function date(value) {
  if (!value) return 'Não informado';
  return dateFormatter.format(new Date(value.length === 10 ? `${value}T12:00:00` : value));
}
export function compactDate(value) {
  return value ? value.slice(0, 4) : '';
}
