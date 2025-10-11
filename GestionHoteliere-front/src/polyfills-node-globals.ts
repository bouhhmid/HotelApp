// Polyfills pour libs "node-like" dans le navigateur
// Corrige: "global is not defined", et évite des erreurs sur process.env

// global -> window
(window as any).global = (window as any).global || window;

// process.env (limité au strict nécessaire)
(window as any).process = (window as any).process || { env: { DEBUG: undefined } };

// Si plus tard une lib réclame Buffer, décommente ces 2 lignes
// import { Buffer } from 'buffer';
// (window as any).Buffer = (window as any).Buffer || Buffer;
