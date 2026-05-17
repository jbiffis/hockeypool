// Round deadlines are stored on the server as naive LocalDateTime values representing wall-clock
// time in Eastern Time (America/Toronto). Rendering with `new Date(...)` would reinterpret them in
// the browser's local timezone, so we parse the components directly and append an explicit ET label.
export function formatDeadlineEt(dateStr) {
  if (!dateStr) return '';
  const m = dateStr.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/);
  if (!m) return dateStr;
  const [, yStr, moStr, dStr, hStr, miStr] = m;
  const y = Number(yStr);
  const mo = Number(moStr);
  const d = Number(dStr);
  const h = Number(hStr);
  const mi = Number(miStr);
  const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  const ref = new Date(y, mo - 1, d, h, mi);
  const hour12 = ((h + 11) % 12) + 1;
  const ampm = h < 12 ? 'AM' : 'PM';
  return `${days[ref.getDay()]}, ${months[mo - 1]} ${d}, ${y} at ${hour12}:${String(mi).padStart(2, '0')} ${ampm} ET`;
}
