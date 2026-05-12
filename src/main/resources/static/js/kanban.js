let currentTask = null;
let taskModal = null;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Kanban board initialized');
    taskModal = new bootstrap.Modal(document.getElementById('taskModal'));
    
    // Add click handlers to task cards
    document.querySelectorAll('.task-card').forEach(card => {
        card.addEventListener('click', function() {
            handleTaskClick(this);
        });
    });
    
    // Save task button
    document.getElementById('saveTask').addEventListener('click', saveTask);
    
    // Complete task button
    document.getElementById('completeTask').addEventListener('click', completeTask);
    
    // Delete task button
    document.getElementById('deleteTask').addEventListener('click', deleteTask);
    
    // Check current tasks on page
    console.log('Tasks on page:', document.querySelectorAll('.task-card').length);
    
    // Refresh immediately, then every 30 seconds
    refreshBoard();
    setInterval(refreshBoard, 30000);
});

function handleTaskClick(taskElement) {
    const taskId = taskElement.dataset.taskId;
    const isTaskInProgress = taskElement.classList.contains('in-progress');
    
    if (isTaskInProgress) {
        // If task is in progress, show modal with complete button
        openTaskModal(taskId);
    } else {
        // Toggle task status (ACTIVE <-> IN_PROGRESS)
        toggleTaskStatus(taskId);
    }
}

function toggleTaskStatus(taskId) {
    fetch(`/api/tasks/${taskId}/toggle`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Failed to toggle task status');
    })
    .then(task => {
        updateTaskCard(task);
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при изменении статуса задачи');
    });
}

function openTaskModal(taskId) {
    fetch(`/api/tasks/${taskId}`)
        .then(response => response.json())
        .then(task => {
            currentTask = task;
            populateTaskForm(task);
            showTaskModal(task.status === 'IN_PROGRESS');
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ошибка при загрузке задачи');
        });
}

function openNewTaskModal() {
    currentTask = null;
    clearTaskForm();
    showTaskModal(false);
}

function populateTaskForm(task) {
    document.getElementById('taskId').value = task.id;
    document.getElementById('taskTitle').value = task.title;
    document.getElementById('taskDescription').value = task.description || '';
    document.getElementById('taskDay').value = task.dayOfWeek;
    
    if (task.deadline) {
        const deadline = new Date(task.deadline);
        document.getElementById('taskDeadline').value = deadline.toISOString().slice(0, 16);
    }
    
    if (task.assignedUser) {
        document.getElementById('taskUser').value = task.assignedUser.id;
    }
}

function clearTaskForm() {
    document.getElementById('taskId').value = '';
    document.getElementById('taskTitle').value = '';
    document.getElementById('taskDescription').value = '';
    document.getElementById('taskDay').value = 'MONDAY';
    document.getElementById('taskDeadline').value = '';
    document.getElementById('taskUser').value = '';
}

function showTaskModal(showCompleteButton) {
    document.getElementById('completeTask').classList.toggle('d-none', !showCompleteButton);
    
    // Show/hide delete button based on user role (simplified)
    const isAdmin = document.querySelector('[data-user-role="ADMIN"]') !== null;
    document.getElementById('deleteTask').classList.toggle('d-none', !isAdmin);
    
    taskModal.show();
}

function saveTask() {
    const taskData = {
        title: document.getElementById('taskTitle').value,
        description: document.getElementById('taskDescription').value,
        dayOfWeek: document.getElementById('taskDay').value,
        deadline: document.getElementById('taskDeadline').value ? new Date(document.getElementById('taskDeadline').value) : null,
        assignedUser: document.getElementById('taskUser').value ? { id: parseInt(document.getElementById('taskUser').value) } : null
    };
    
    const taskId = document.getElementById('taskId').value;
    const url = taskId ? `/api/tasks/${taskId}` : '/api/tasks';
    const method = taskId ? 'PUT' : 'POST';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(taskData)
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Failed to save task');
    })
    .then(task => {
        if (taskId) {
            updateTaskCard(task);
        } else {
            addTaskCard(task);
        }
        taskModal.hide();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при сохранении задачи');
    });
}

function completeTask() {
    if (!currentTask) return;
    
    fetch(`/api/tasks/${currentTask.id}/complete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Failed to complete task');
    })
    .then(task => {
        updateTaskCard(task);
        taskModal.hide();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при выполнении задачи');
    });
}

function deleteTask() {
    if (!currentTask) return;
    
    if (!confirm('Вы уверены, что хотите удалить эту задачу?')) return;
    
    fetch(`/api/tasks/${currentTask.id}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            removeTaskCard(currentTask.id);
            taskModal.hide();
        } else {
            throw new Error('Failed to delete task');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при удалении задачи');
    });
}

function updateTaskCard(task) {
    const taskElement = document.querySelector(`[data-task-id="${task.id}"]`);
    if (taskElement) {
        // Update content
        taskElement.querySelector('.task-title').textContent = task.title;
        taskElement.querySelector('.task-description').textContent = task.description ? 
            task.description.substring(0, 50) + (task.description.length > 50 ? '...' : '') : '';
        
        // Update assigned user
        const assignedUserElement = taskElement.querySelector('.assigned-user');
        if (task.assignedUser) {
            if (assignedUserElement) {
                assignedUserElement.textContent = task.assignedUser.login;
                assignedUserElement.style.display = 'inline';
            } else {
                const header = taskElement.querySelector('.task-header');
                const userSpan = document.createElement('span');
                userSpan.className = 'assigned-user';
                userSpan.textContent = task.assignedUser.login;
                header.appendChild(userSpan);
            }
        } else if (assignedUserElement) {
            assignedUserElement.style.display = 'none';
        }
        
        // Update deadline
        const deadlineElement = taskElement.querySelector('.deadline');
        if (task.deadline) {
            deadlineElement.textContent = new Date(task.deadline).toLocaleTimeString('ru-RU', { 
                hour: '2-digit', 
                minute: '2-digit' 
            });
            deadlineElement.style.display = 'inline';
        } else if (deadlineElement) {
            deadlineElement.style.display = 'none';
        }
        
        // Update status and color
        taskElement.querySelector('.status-badge').textContent = task.status;
        taskElement.className = 'task-card ' + task.colorClass;
        
        // If task is completed, remove it after a delay
        if (task.status === 'COMPLETED') {
            setTimeout(() => removeTaskCard(task.id), 2000);
        }
    }
}

function addTaskCard(task) {
    const dayContainer = document.querySelector(`[data-day="${task.dayOfWeek}"]`);
    if (dayContainer) {
        const taskCard = createTaskCardElement(task);
        dayContainer.appendChild(taskCard);
        
        // Update day counter
        const dayColumn = dayContainer.closest('.day-column');
        const badge = dayColumn.querySelector('.badge');
        const currentCount = parseInt(badge.textContent);
        badge.textContent = currentCount + 1;
    }
}

function removeTaskCard(taskId) {
    const taskElement = document.querySelector(`[data-task-id="${taskId}"]`);
    if (taskElement) {
        const dayContainer = taskElement.parentElement;
        const dayColumn = dayContainer.closest('.day-column');
        const badge = dayColumn.querySelector('.badge');
        const currentCount = parseInt(badge.textContent);
        badge.textContent = Math.max(0, currentCount - 1);
        
        taskElement.remove();
    }
}

function createTaskCardElement(task) {
    const div = document.createElement('div');
    div.className = 'task-card ' + task.colorClass;
    div.dataset.taskId = task.id;
    
    div.innerHTML = `
        <div class="task-header">
            <h6 class="task-title">${task.title}</h6>
            ${task.assignedUser ? `<span class="assigned-user">${task.assignedUser.login}</span>` : ''}
        </div>
        ${task.description ? `<div class="task-description">${task.description.substring(0, 50)}${task.description.length > 50 ? '...' : ''}</div>` : ''}
        <div class="task-footer">
            ${task.deadline ? `<small class="deadline">${new Date(task.deadline).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}</small>` : ''}
            <span class="badge status-badge ${task.status}">${task.status}</span>
        </div>
    `;
    
    div.addEventListener('click', function() {
        handleTaskClick(this);
    });
    
    return div;
}

function refreshBoard() {
    console.log('Refreshing board...');
    fetch('/api/tasks')
        .then(response => {
            console.log('API response status:', response.status);
            return response.json();
        })
        .then(tasksByDay => {
            console.log('Tasks by day:', tasksByDay);
            // Update all task cards
            Object.entries(tasksByDay).forEach(([day, tasks]) => {
                console.log(`Processing day: ${day}, tasks count: ${tasks.length}`);
                const dayContainer = document.querySelector(`[data-day="${day}"]`);
                console.log(`Day container for ${day}:`, dayContainer);
                if (dayContainer) {
                    // Clear existing tasks
                    dayContainer.innerHTML = '';
                    
                    // Add updated tasks
                    tasks.forEach(task => {
                        dayContainer.appendChild(createTaskCardElement(task));
                    });
                    
                    // Update counter
                    const dayColumn = dayContainer.closest('.day-column');
                    const badge = dayColumn.querySelector('.badge');
                    badge.textContent = tasks.length;
                }
            });
        })
        .catch(error => {
            console.error('Error refreshing board:', error);
        });
}

// Utility function to format date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU') + ' ' + date.toLocaleTimeString('ru-RU', { 
        hour: '2-digit', 
        minute: '2-digit' 
    });
}
