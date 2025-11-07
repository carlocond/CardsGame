const baseUrl = window.location.origin;

function out(msg){
  document.getElementById('output').textContent = JSON.stringify(msg, null, 2);
}

async function login(){
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;
  const res = await fetch(baseUrl + '/api/v1/auth/login', {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify({email, password})
  });
  const json = await res.json();
  if (res.ok) {
    document.getElementById('token').value = json.token;
    out(json);
  } else {
    out({status: res.status, body: json});
  }
}

async function register(){
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;
  const res = await fetch(baseUrl + '/api/v1/auth/register', {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify({fName:'Test', lName:'User', email, password})
  });
  const json = await res.json();
  out(json);
  if (res.ok) document.getElementById('token').value = json.token;
}

async function listTemplates(){
  const token = document.getElementById('token').value;
  const res = await fetch(baseUrl + '/api/pack-templates', {
    headers: {'Authorization':'Bearer ' + token}
  });
  const json = await res.json();
  out(json);
}

async function openPack(){
  const token = document.getElementById('token').value;
  const res = await fetch(baseUrl + '/api/pack-openings/1/open?userId=1', {
    method: 'POST',
    headers: {'Authorization':'Bearer ' + token}
  });
  const json = await res.json();
  out(json);
}

document.getElementById('btnLogin').addEventListener('click', login);
document.getElementById('btnRegister').addEventListener('click', register);
document.getElementById('btnListTemplates').addEventListener('click', listTemplates);
document.getElementById('btnOpenPack').addEventListener('click', openPack);

