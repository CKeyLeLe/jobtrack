import { FormControl, FormGroup } from '@angular/forms';
import { addDays, today } from '../core/dates';
import { followUpNotBeforeApplied, notInFuture } from './application-validators';

describe('application validators', () => {
  it('rejects an application date in the future', () => {
    expect(notInFuture(new FormControl(addDays(today(), 1)))).toEqual({ futureDate: true });
    expect(notInFuture(new FormControl(today()))).toBeNull();
  });

  it('rejects a follow-up before the application date', () => {
    // As in the app: the form is built first, then the user types (the validator needs the parent).
    const form = new FormGroup({
      appliedOn: new FormControl('2026-11-02'),
      followUpOn: new FormControl('', followUpNotBeforeApplied),
    });

    form.controls.followUpOn.setValue('2026-11-01');
    expect(form.controls.followUpOn.errors).toEqual({ beforeApplication: true });

    form.controls.followUpOn.setValue('2026-11-02');
    expect(form.controls.followUpOn.errors).toBeNull();
  });

  it('accepts an empty follow-up', () => {
    const form = new FormGroup({
      appliedOn: new FormControl('2026-11-02'),
      followUpOn: new FormControl('', followUpNotBeforeApplied),
    });

    expect(form.controls.followUpOn.valid).toBe(true);
  });
});
