const express = require('express');
const app = express();

app.use(express.json()); // parse JSON request body

app.post('/chat', (req, res) => {
  const userMessage = req.body.message;
  console.log('Received message:', userMessage);
  res.json({ response: `You said: ${userMessage}` });
});

app.listen(5000, '0.0.0.0', () => {
  console.log('Server running on http://0.0.0.0:5000');
});
