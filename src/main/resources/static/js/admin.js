const baseUrl = window.location.origin;

function adminOut(msg){
  document.getElementById('adminOutput').textContent = JSON.stringify(msg, null, 2);
}

async function addCard(){
  const token = document.getElementById('adminToken').value;
  const body = {
    name: 'AdminCard',
    description: 'Created from admin panel',
    type: 'Normal',
    rarity: 'COMMON',
    imageUrl: 'url/admin.png',
    expansion: { id: 1 }
  };

  const res = await fetch(baseUrl + '/api/admin/cards', {
    method: 'POST',
    headers: {
      'Content-Type':'application/json',
      'Authorization':'Bearer ' + token
    },
    body: JSON.stringify(body)
  });
  if (res.status === 200) adminOut({status:200, msg:'Card added'});
  else adminOut({status: res.status, body: await res.text()});
}

document.getElementById('btnAddCard').addEventListener('click', addCard);

