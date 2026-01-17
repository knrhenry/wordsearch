document.getElementById('wordsearch-form').addEventListener('submit', async function(e) {
    e.preventDefault();
    const words = document.getElementById('words').value;
    const pdf = document.getElementById('pdf').checked;
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = 'Loading...';

    const apiUrl = 'https://89mpbnacq1.execute-api.us-west-2.amazonaws.com/wordsearch';

    try {
        const response = await fetch(apiUrl + (pdf ? '?pdf=true' : ''), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ words: words.split(',').map(w => w.trim()) })
        });
        if (pdf) {
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            resultDiv.innerHTML = `<a href="${url}" target="_blank">View PDF</a>`;
        } else {
            const data = await response.json();
            let grid = data.grid;
            // Handle grid as string or array
            if (typeof grid === 'string') {
                // Split by newlines, then by characters or spaces
                grid = grid.split('\n').map(row => row.split(/\s+/));
            }
            let html = '<h2>Word Search Grid</h2><table class="grid">';
            grid.forEach(row => {
                // If row is a string, split into characters
                if (typeof row === 'string') {
                    row = row.split('');
                }
                html += '<tr>' + row.map(cell => `<td>${cell}</td>`).join('') + '</tr>';
            });
            html += '</table>';
            html += '<h3>Words:</h3><ul>' + data.words.map(w => `<li>${w}</li>`).join('') + '</ul>';
            resultDiv.innerHTML = html;
        }
    } catch (err) {
        resultDiv.innerHTML = 'Error: ' + err;
    }
});
