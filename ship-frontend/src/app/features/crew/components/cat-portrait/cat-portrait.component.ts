import { Component, computed, input } from '@angular/core';
import { Coat, Eyes, Fur, Portrait } from '../../../ships/models/portrait';

const FUR_COLOURS: Record<Fur, string> = {
  ginger: '#D98B3A',
  grey: '#8E959C',
  black: '#2B2B30',
  cream: '#EBD9B0',
  brown: '#7A5233',
  white: '#F7F3EA',
};

const EYE_COLOURS: Record<Eyes, string> = {
  green: '#4F9A5B',
  amber: '#D9A030',
  blue: '#4B86B8',
};

/** Marking colour of each coat: a darker or lighter tone of the fur. */
const COAT_MARKS: Record<Coat, boolean> = { plain: false, tabby: true, tuxedo: true, spotted: true };

/** A cat head as an inline SVG, drawn from a Recruit Portrait, in a brass ring. */
@Component({
  selector: 'app-cat-portrait',
  templateUrl: './cat-portrait.component.html',
  styleUrl: './cat-portrait.component.scss',
})
export class CatPortraitComponent {
  portrait = input.required<Portrait>();
  name = input.required<string>();
  ring = input(false);
  /** True when the name is shown next to the portrait: hides it from assistive technology. */
  decorative = input(false);

  fur = computed(() => FUR_COLOURS[this.portrait().fur]);
  eyeColour = computed(() => EYE_COLOURS[this.portrait().eyes]);
  hasMarks = computed(() => COAT_MARKS[this.portrait().coat]);
}
