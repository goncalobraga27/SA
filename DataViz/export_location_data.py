from google.cloud import firestore
from google.oauth2 import service_account
import gspread

# Configure seu arquivo de credenciais JSON do Google Cloud
credentials = service_account.Credentials.from_service_account_file('projeto-sa-b15e2-firebase-adminsdk-2sue7-b315f010a6.json')

# Inicialize o cliente Firestore
db = firestore.Client(credentials=credentials, project=credentials.project_id)

def get_user_locations():
    users_ref = db.collection('users')
    users_locations = []
    for user in users_ref.stream():
        locations_ref = user.reference.collection('user_location')
        for location in locations_ref.stream():
            users_locations.append(location.to_dict())
    return users_locations

gc = gspread.service_account(filename='projeto-sa-b15e2-firebase-adminsdk-2sue7-b315f010a6.json')
sh = gc.open_by_key('1ZD9IFn7gLGmDUl9PKIyd_qXKFofpVwaP5mUvP-zunaE') 
worksheet = sh.get_worksheet(0) 

def export_to_sheets(data):
    # Prepara uma lista de valores a serem inseridos
    values = [['Location']] + [[f"{item['latitude']}, {item['longitude']}"] for item in data]
    # Limpa a planilha e insere os novos dados
    worksheet.clear()
    worksheet.update(values, 'A1')

# Coleta os dados
data = get_user_locations()
# Exporta os dados para a planilha
export_to_sheets(data)
