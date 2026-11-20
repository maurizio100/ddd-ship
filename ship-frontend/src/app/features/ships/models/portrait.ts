export type Fur = 'ginger' | 'grey' | 'black' | 'cream' | 'brown' | 'white';
export type Coat = 'plain' | 'tabby' | 'tuxedo' | 'spotted';
export type Eyes = 'green' | 'amber' | 'blue';
export type Accessory = 'none' | 'eye-patch' | 'bandana' | 'earring';

/** The look of a Recruit and of the Crew Member it becomes (the Recruit Portrait); derived by the backend from the cat's UUID. */
export interface Portrait {
  fur: Fur;
  coat: Coat;
  eyes: Eyes;
  accessory: Accessory;
}
