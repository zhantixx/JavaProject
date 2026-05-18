const KAZAKH_MONTHS = [
  'қаңтар',
  'ақпан',
  'наурыз',
  'сәуір',
  'мамыр',
  'маусым',
  'шілде',
  'тамыз',
  'қыркүйек',
  'қазан',
  'қараша',
  'желтоқсан',
];

function asDate(value) {
  if (!value) return null;
  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatMonthYear(value) {
  const date = asDate(value);
  if (!date) return 'Көрсетілмеген';
  return `${KAZAKH_MONTHS[date.getMonth()]} ${date.getFullYear()}`;
}

export function formatLongDate(value) {
  const date = asDate(value);
  if (!date) return 'Көрсетілмеген';
  return `${date.getDate()} ${KAZAKH_MONTHS[date.getMonth()]} ${date.getFullYear()}`;
}

export function formatLongDateTime(value) {
  const date = asDate(value);
  if (!date) return 'Көрсетілмеген';
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${date.getDate()} ${KAZAKH_MONTHS[date.getMonth()]} ${date.getFullYear()}, ${hours}:${minutes}`;
}
