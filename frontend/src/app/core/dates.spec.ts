import { addDays, daysBetween, toIsoDate } from './dates';

describe('dates', () => {
  it('formats a local date as YYYY-MM-DD', () => {
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('adds days across month and year boundaries', () => {
    expect(addDays('2026-12-28', 7)).toBe('2027-01-04');
  });

  it('counts whole days, ignoring daylight saving changes', () => {
    expect(daysBetween('2026-03-25', '2026-04-01')).toBe(7);
    expect(daysBetween('2026-11-12', '2026-11-12')).toBe(0);
  });
});
