const express = require('express');
const bodyParser = require('body-parser');
const puppeteer = require('puppeteer');

const app = express();
app.use(bodyParser.json({ limit: '5mb' }));

app.get('/ping', (req, res) => res.send('ok'));

app.post('/render', async (req, res) => {
  const { url, width = 800, height = 600, fullPage = false } = req.body;
  if (!url) return res.status(400).send('missing url');
  try {
    const browser = await puppeteer.launch({ args: ['--no-sandbox','--disable-setuid-sandbox'] });
    const page = await browser.newPage();
    await page.setViewport({ width: parseInt(width), height: parseInt(height) });
    await page.goto(url, { waitUntil: 'networkidle2', timeout: 20000 });
    const buffer = await page.screenshot({ fullPage: !!fullPage, type: 'png' });
    await browser.close();
    res.set('Content-Type', 'image/png');
    res.send(buffer);
  } catch (e) {
    console.error('render error', e);
    res.status(500).send('render failed: ' + e.message);
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log('WebRender renderer listening on', port));
