import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CatPortraitComponent } from './cat-portrait.component';
import { Accessory, Coat, Eyes, Fur, Portrait } from '../../../ships/models/portrait';

describe('CatPortraitComponent', () => {
  let fixture: ComponentFixture<CatPortraitComponent>;
  const base: Portrait = { fur: 'ginger', coat: 'plain', eyes: 'green', accessory: 'none' } as const;

  function render(portrait: Portrait, name = 'Mittens', ring = false): SVGElement {
    fixture = TestBed.createComponent(CatPortraitComponent);
    fixture.componentRef.setInput('portrait', portrait);
    fixture.componentRef.setInput('name', name);
    fixture.componentRef.setInput('ring', ring);
    fixture.detectChanges();
    return fixture.nativeElement.querySelector('svg');
  }

  it('draws an svg image labelled with the name of the cat', () => {
    const svg = render(base, 'Mittens');

    expect(svg.getAttribute('role')).toBe('img');
    expect(svg.getAttribute('aria-label')).toBe('Portrait of Mittens');
  });

  it('draws a brass ring only when asked to', () => {
    expect(render(base).querySelector('.portrait__ring')).toBeNull();
    expect(render(base, 'Mittens', true).querySelector('.portrait__ring')).not.toBeNull();
  });

  it('draws different keys differently', () => {
    const a = render(base).innerHTML;
    const b = render({ fur: 'black', coat: 'tabby', eyes: 'blue', accessory: 'bandana' }).innerHTML;

    expect(a).not.toEqual(b);
  });

  it('renders every fur, coat, eyes and accessory', () => {
    const furs: Fur[] = ['ginger', 'grey', 'black', 'cream', 'brown', 'white'];
    const coats: Coat[] = ['plain', 'tabby', 'tuxedo', 'spotted'];
    const eyes: Eyes[] = ['green', 'amber', 'blue'];
    const accessories: Accessory[] = ['none', 'eye-patch', 'bandana', 'earring'];

    const furFills = furs.map((fur) => render({ ...base, fur }).querySelector('.portrait__fur')!.getAttribute('fill'));
    expect(new Set(furFills).size).toBe(furs.length);
    expect(furFills.every((fill) => /^#[0-9A-F]{6}$/i.test(fill!))).toBeTrue();

    const coatDrawings = coats.map((coat) => render({ ...base, coat }).innerHTML);
    expect(new Set(coatDrawings).size).toBe(coats.length);

    const eyeFills = eyes.map((e) => render({ ...base, eyes: e }).innerHTML);
    expect(new Set(eyeFills).size).toBe(eyes.length);

    const accessoryDrawings = accessories.map((accessory) => render({ ...base, accessory }).innerHTML);
    expect(new Set(accessoryDrawings).size).toBe(accessories.length);
  });
});
