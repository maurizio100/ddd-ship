import { HttpErrorResponse } from '@angular/common/http';
import { aCargo } from '../../../../testing/fixtures';
import { loadRejectionMessage } from './load-rejection';

describe('loadRejectionMessage', () => {
  const rum = aCargo({ id: 'rum', name: 'Rum' });
  const problem = (title: string, detail?: string) =>
    new HttpErrorResponse({ status: 409, error: { title, status: 409, detail } });

  it('explains a ship that would get too heavy with one decimal for the Max Weight', () => {
    expect(loadRejectionMessage(problem('Ship too heavy', 'other'), rum, 15)).toBe(
      'Rum would exceed the Max Weight of 15.0'
    );
  });

  it('explains Cargo that is already loaded', () => {
    expect(loadRejectionMessage(problem('Cargo already loaded', 'other'), rum, 15)).toBe('Rum is already loaded');
  });

  it('explains Cargo that ran out of Stock', () => {
    expect(loadRejectionMessage(problem('Cargo out of Stock', 'other'), rum, 15)).toBe('Rum is out of Stock');
  });

  it('falls back to the detail of an unknown problem', () => {
    expect(loadRejectionMessage(problem('Something else', 'Try later'), rum, 15)).toBe('Try later');
  });

  it('falls back to a generic text without Problem Details', () => {
    expect(loadRejectionMessage(new HttpErrorResponse({ status: 500 }), rum, 15)).toBe(
      'The Cargo could not be loaded'
    );
  });
});
