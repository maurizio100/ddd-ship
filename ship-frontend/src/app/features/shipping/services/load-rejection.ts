import { HttpErrorResponse } from '@angular/common/http';
import { Cargo } from '../models/cargo';

const LOAD_REJECTED = 'The Cargo could not be loaded';

/**
 * Words a rejected load for the User. The backend answers 409 Problem Details whose `title` names the
 * rule that was broken (see ProblemDetailsExceptionHandler); its `detail` is the fallback.
 */
export function loadRejectionMessage(error: HttpErrorResponse, cargo: Cargo, maxWeight: number): string {
  switch (error.error?.title) {
    case 'Ship too heavy':
      return `${cargo.name} would exceed the Max Weight of ${maxWeight.toFixed(1)}`;
    case 'Cargo already loaded':
      return `${cargo.name} is already loaded`;
    case 'Cargo out of Stock':
      return `${cargo.name} is out of Stock`;
    default:
      return error.error?.detail ?? LOAD_REJECTED;
  }
}
