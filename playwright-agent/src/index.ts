import { AutomationAgent } from './agent';
import { NaukriStrategy } from './portals/naukri';
import { FounditStrategy } from './portals/foundit';
import dotenv from 'dotenv';
import http from 'http';

dotenv.config();

const port = process.env.PORT || 3000;
http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end('Playwright Agent is running\n');
}).listen(port, () => {
  console.log(`Health check server listening on port ${port}`);
});

async function main() {
  console.log('Starting JOB AI Playwright Automation Agent...');
  
  const agent = new AutomationAgent();
  agent.registerStrategy(NaukriStrategy);
  agent.registerStrategy(FounditStrategy);
  // Add other strategies here...

  const pollIntervalMs = parseInt(process.env.POLL_INTERVAL_MS || '60000', 10);

  // Initial run
  await agent.run();

  // Schedule regular polling
  setInterval(async () => {
    try {
      await agent.run();
    } catch (error) {
      console.error('Unhandled error during agent run:', error);
    }
  }, pollIntervalMs);
  
  console.log(`Agent is polling every ${pollIntervalMs / 1000} seconds for new applications...`);
}

main().catch(console.error);
