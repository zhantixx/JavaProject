export function isSameDay(left, right) {
  return left.toDateString() === right.toDateString();
}

export function getWeekBounds(date = new Date()) {
  const current = new Date(date);
  const day = (current.getDay() + 6) % 7;
  const start = new Date(current);
  start.setHours(0, 0, 0, 0);
  start.setDate(current.getDate() - day);

  const end = new Date(start);
  end.setDate(start.getDate() + 6);
  end.setHours(23, 59, 59, 999);
  return { start, end };
}

export function getMonthBounds(date = new Date()) {
  const start = new Date(date.getFullYear(), date.getMonth(), 1, 0, 0, 0, 0);
  const end = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59, 999);
  return { start, end };
}

export function getYearBounds(date = new Date()) {
  const start = new Date(date.getFullYear(), 0, 1, 0, 0, 0, 0);
  const end = new Date(date.getFullYear(), 11, 31, 23, 59, 59, 999);
  return { start, end };
}

export function taskInRange(task, start, end) {
  if (!task?.deadline) return false;
  const deadline = new Date(task.deadline);
  return deadline >= start && deadline <= end;
}

export function buildPlanSummary(tasks, start, end) {
  const scoped = tasks.filter((task) => taskInRange(task, start, end));
  const done = scoped.filter((task) => task.status === 'DONE').length;
  const high = scoped.filter((task) => task.priority === 'HIGH').length;
  const open = scoped.filter((task) => task.status !== 'DONE' && task.status !== 'CANCELED').length;

  return {
    total: scoped.length,
    done,
    high,
    open,
    completionRate: scoped.length ? Math.round((done / scoped.length) * 100) : 0,
    items: scoped.sort((a, b) => new Date(a.deadline) - new Date(b.deadline)),
  };
}

export function buildProductivityTrackers(tasks) {
  const total = tasks.length;
  const done = tasks.filter((task) => task.status === 'DONE').length;
  const withDeadline = tasks.filter((task) => Boolean(task.deadline)).length;
  const active = tasks.filter((task) => task.status !== 'DONE' && task.status !== 'CANCELED');
  const overdue = active.filter((task) => task.deadline && new Date(task.deadline) < new Date()).length;
  const high = active.filter((task) => task.priority === 'HIGH').length;

  return {
    completionRate: total ? Math.round((done / total) * 100) : 0,
    planningCoverage: total ? Math.round((withDeadline / total) * 100) : 0,
    reliabilityScore: active.length ? Math.max(0, 100 - Math.round((overdue / active.length) * 100)) : 100,
    focusLoad: high,
  };
}
