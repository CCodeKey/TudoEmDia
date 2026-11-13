import * as verificadoresDom from '@testing-library/jest-dom/matchers';
import { cleanup } from '@testing-library/react';

// Registrados no expect global (e não via "@testing-library/jest-dom/vitest"), para funcionar
// mesmo quando o Windows abre o projeto com "c:" minúsculo no caminho
expect.extend(verificadoresDom);

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});
