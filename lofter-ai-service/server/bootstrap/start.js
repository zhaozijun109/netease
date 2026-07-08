const { execSync } = require("node:child_process");
const path = require("node:path");

const repoRoot = path.resolve(__dirname, "../..");
process.chdir(repoRoot);

execSync("node server.js", {
  cwd: repoRoot,
  stdio: "inherit",
  env: { ...process.env, NODE_ENV: "production", PORT: "3000", HOSTNAME: "0.0.0.0" },
});
