const { execSync } = require("node:child_process");

const port = process.env.PORT || 3000;

try {
  execSync(`lsof -ti tcp:${port} | xargs kill -SIGTERM`, { stdio: "inherit" });
  console.log(`Stopped service on port ${port}`);
} catch {
  console.log(`No process found on port ${port}`);
}
