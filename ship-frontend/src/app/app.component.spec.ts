import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {AppComponent} from './app.component';
import {provideMaterialSymbols} from './app.config';
import {HarborNameService} from './core/harbor-name.service';
import {Observable, of, throwError} from 'rxjs';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;

  const byTestId = (id: string): HTMLElement | null =>
    fixture.nativeElement.querySelector(`[data-testid="${id}"]`);

  const setUp = async (harborName: Observable<string>) => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideMaterialSymbols(),
        provideRouter([{path: 'ships', children: []}, {path: 'ships/:id/cargo', children: []}, {path: 'harbor', children: []}]),
        {provide: HarborNameService, useValue: {getHarborName: () => harborName}}
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
  };

  beforeEach(() => setUp(of('Port Royal')));

  it('renders the app bar with the Hexagonship wordmark', () => {
    expect(byTestId('app-bar')).not.toBeNull();
    expect(byTestId('wordmark')?.textContent?.trim()).toBe('Hexagonship');
  });

  it('shows a Fleet navigation link to the ships list', () => {
    const fleet = byTestId('nav-fleet') as HTMLAnchorElement;
    expect(fleet.textContent?.trim()).toBe('Fleet');
    expect(fleet.getAttribute('href')).toBe('/ships');
  });

  it('shows a Harbor navigation link to the harbor page', () => {
    const harbor = byTestId('nav-harbor') as HTMLAnchorElement;
    expect(harbor.textContent?.trim()).toBe('Harbor');
    expect(harbor.getAttribute('href')).toBe('/harbor');
  });

  it('marks the Harbor pill active on the harbor route', async () => {
    await TestBed.inject(Router).navigateByUrl('/harbor');
    fixture.detectChanges();
    expect(byTestId('nav-harbor')?.classList).toContain('active');
    expect(byTestId('nav-fleet')?.classList).not.toContain('active');
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
    expect(icon.classList).toContain('mat-ligature-font');
  });

  it('shows the Harbor Name in the app bar', () => {
    expect(byTestId('app-harbor-name')?.textContent?.trim()).toBe('Harbor of Port Royal');
  });

  it('keeps the Harbor Name in the app bar on the cargo route', async () => {
    await TestBed.inject(Router).navigateByUrl('/ships/1/cargo');
    fixture.detectChanges();
    expect(byTestId('app-harbor-name')?.textContent?.trim()).toBe('Harbor of Port Royal');
  });
});

describe('AppComponent Harbor Name variants', () => {
  let fixture: ComponentFixture<AppComponent>;
  const byTestId = (id: string): HTMLElement | null =>
    fixture.nativeElement.querySelector(`[data-testid="${id}"]`);

  const setUp = async (harborName: Observable<string>) => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideMaterialSymbols(),
        provideRouter([{path: 'ships', children: []}]),
        {provide: HarborNameService, useValue: {getHarborName: () => harborName}}
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
  };

  it('shows only its own Harbor Name', async () => {
    await setUp(of('Tortuga'));
    expect(fixture.nativeElement.textContent).toContain('Tortuga');
    expect(fixture.nativeElement.textContent).not.toContain('Port Royal');
  });

  it('shows no Harbor Name and the app bar still renders when the lookup fails', async () => {
    await setUp(throwError(() => new Error('boom')));
    expect(byTestId('app-harbor-name')).toBeNull();
    expect(byTestId('wordmark')).not.toBeNull();
    expect(byTestId('nav-fleet')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('router-outlet')).not.toBeNull();
  });
});
