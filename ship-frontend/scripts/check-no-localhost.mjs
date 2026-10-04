// Fails when the production bundle still contains the development backend URL.
import { readdirSync, readFileSync, statSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const FORBIDDEN = 'localhost:8080';
const distRoot = 'dist';

function walk(dir) {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? walk(path) : [path];
  });
}

if (!existsSync(distRoot)) {
  console.error(`No ${distRoot}/ folder found; run the build first.`);
  process.exit(1);
}

const browserDirs = readdirSync(distRoot)
  .map((project) => join(distRoot, project, 'browser'))
  .filter((dir) => existsSync(dir));

const offenders = browserDirs
  .flatMap(walk)
  .filter((file) => readFileSync(file, 'utf8').includes(FORBIDDEN));

if (offenders.length > 0) {
  console.error(`Production bundle contains "${FORBIDDEN}" in:\n  ${offenders.join('\n  ')}`);
  process.exit(1);
}
console.log(`Production bundle is free of "${FORBIDDEN}".`);
