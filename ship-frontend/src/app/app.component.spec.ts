import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {AppComponent} from './app.component';
import {provideMaterialSymbols} from './app.config';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;

  const byTestId = (id: string): HTMLElement | null =>
    fixture.nativeElement.querySelector(`[data-testid="${id}"]`);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideMaterialSymbols(), provideRouter([{path: 'ships', children: []}])]
    }).compileComponents();
    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
  });

  it('renders the app bar with the Hexagonship wordmark', () => {
    expect(byTestId('app-bar')).not.toBeNull();
    expect(byTestId('wordmark')?.textContent?.trim()).toBe('Hexagonship');
  });

  it('shows a Fleet navigation link to the ships list and no Harbor entry', () => {
    const fleet = byTestId('nav-fleet') as HTMLAnchorElement;
    expect(fleet.textContent?.trim()).toBe('Fleet');
    expect(fleet.getAttribute('href')).toBe('/ships');
    expect(fixture.nativeElement.querySelector('nav')?.textContent).not.toContain('Harbor');
  });

  it('marks the Fleet pill active on the ships list route', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/ships');
    fixture.detectChanges();
    expect(byTestId('nav-fleet')?.classList).toContain('active');
  });

  it('renders icons with the Material Symbols Outlined font set', () => {
    const icon = fixture.nativeElement.querySelector('mat-icon') as HTMLElement;
    expect(icon.classList).toContain('material-symbols-outlined');
    expect(icon.classList).not.toContain('material-icons');
  });
});
