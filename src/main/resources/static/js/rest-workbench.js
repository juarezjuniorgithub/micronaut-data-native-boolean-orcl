(() => {
    const result = document.getElementById('api-result');
    const resultStatus = document.getElementById('api-result-status');
    const taskId = document.getElementById('api-task-id');
    const taskResponseRows = document.getElementById('task-response-rows');

    if (!result || !resultStatus || !taskId || !taskResponseRows) {
        return;
    }

    const isTaskRecord = (value) => value
        && typeof value === 'object'
        && Object.hasOwn(value, 'id')
        && Object.hasOwn(value, 'title')
        && Object.hasOwn(value, 'completed');

    const taskRecordsFrom = (body) => (Array.isArray(body) ? body : [body]).filter(isTaskRecord);

    const textCell = (value) => {
        const cell = document.createElement('td');
        cell.textContent = value == null ? '—' : String(value);
        return cell;
    };

    const renderTaskTable = (body, emptyMessage = 'This response does not contain task records.') => {
        const records = taskRecordsFrom(body);
        taskResponseRows.replaceChildren();
        if (records.length === 0) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = 3;
            cell.className = 'empty-state';
            cell.textContent = emptyMessage;
            row.append(cell);
            taskResponseRows.append(row);
            return;
        }

        records.forEach((task) => {
            const row = document.createElement('tr');
            row.append(textCell(task.id), textCell(task.title));
            const completionCell = document.createElement('td');
            const status = document.createElement('span');
            status.className = `status-chip ${task.completed ? 'is-true' : 'is-false'}`;
            status.textContent = task.completed ? 'TRUE' : 'FALSE';
            completionCell.append(status);
            row.append(completionCell);
            taskResponseRows.append(row);
        });
    };

    const render = (requestLabel, status, body) => {
        const printable = typeof body === 'string' ? body : JSON.stringify(body, null, 2);
        resultStatus.textContent = `${requestLabel} completed — HTTP ${status}`;
        result.textContent = printable;
        renderTaskTable(body);
    };

    const renderFailure = (requestLabel, error) => {
        resultStatus.textContent = `${requestLabel} failed`;
        result.textContent = error instanceof Error ? error.message : String(error);
        renderTaskTable([], 'No task records were returned because the request failed.');
    };

    const request = async (path, options = {}, requestLabel = path) => {
        resultStatus.textContent = `Loading ${requestLabel}…`;
        result.textContent = '';
        renderTaskTable([], 'Loading task response…');
        try {
            const response = await fetch(path, {
                headers: { Accept: 'application/json', ...options.headers },
                ...options
            });
            const text = await response.text();
            let body = text;
            try {
                body = text ? JSON.parse(text) : { message: 'No response body' };
            } catch {
                // Plain-text error responses remain safe because they are rendered with textContent.
            }
            render(requestLabel, response.status, body);
            return { response, body };
        } catch (error) {
            renderFailure(requestLabel, error);
            return { response: null, body: null };
        }
    };

    document.querySelectorAll('[data-api-get]').forEach((button) => {
        button.addEventListener('click', () => request(button.dataset.apiGet, {}, button.textContent.trim()));
    });

    document.querySelectorAll('[data-api-get-template]').forEach((button) => {
        button.addEventListener('click', () => {
            const path = button.dataset.apiGetTemplate.replace('{id}', taskId.value || '1');
            request(path, {}, button.textContent.trim().replace('{id}', taskId.value || '1'));
        });
    });

    document.getElementById('api-create-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const title = document.getElementById('api-title-input').value;
        const completed = document.getElementById('api-completed-input').checked;
        const { body } = await request('/api/tasks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, completed })
        }, 'POST /api/tasks');
        if (body && body.id) {
            taskId.value = body.id;
        }
    });

    document.getElementById('api-update-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        await request(`/api/tasks/${taskId.value}/completed`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ completed: document.getElementById('api-update-completed').checked })
        }, `PUT /api/tasks/${taskId.value}/completed`);
    });
})();
