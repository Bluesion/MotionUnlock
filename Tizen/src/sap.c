#include <glib.h>
#include <sap.h>
#include <sensor.h>
#include "helloaccessory.h"
#include "main_app.h"

#define HELLO_ACC_ASPID "SensorUnlock"
#define HELLO_ACC_CHANNELID 104

extern float HRM;
extern float ACC_X;
extern float ACC_Y;
extern float ACC_Z;
extern float GYR_X;
extern float GYR_Y;
extern float GYR_Z;
extern float PRS;

struct priv {
	sap_agent_h agent;
	sap_socket_h socket;
	sap_peer_agent_h peer_agent;
};

static gboolean agent_created = FALSE;

static struct priv priv_data = { 0 };

static void on_service_connection_terminated(sap_peer_agent_h peer_agent,
					     sap_socket_h socket,
					     sap_service_connection_terminated_reason_e result,
					     void *user_data)
{
	switch (result) {
	case SAP_CONNECTION_TERMINATED_REASON_PEER_DISCONNECTED:
		dlog_print(DLOG_INFO, TAG, "disconnected because peer lost");
		break;

	case SAP_CONNECTION_TERMINATED_REASON_DEVICE_DETACHED:
		dlog_print(DLOG_INFO, TAG, "disconnected because device is detached");
		break;

	case SAP_CONNECTION_TERMINATED_REASON_UNKNOWN:
		dlog_print(DLOG_INFO, TAG, "disconnected because of unknown reason");
		break;
	}

	sap_socket_destroy(priv_data.socket);
	priv_data.socket = NULL;

	dlog_print(DLOG_INFO, TAG, "status:%d", result);
}


static void on_data_recieved(sap_socket_h socket, unsigned short int channel_id, unsigned int payload_length, void *buffer, void *user_data) {
	char a[100] = "";

	char* hrm_msg = (char *) malloc(sizeof(HRM));
	sprintf(hrm_msg, "%.1f", HRM);

	char* acc_msg_x = (char * ) malloc(sizeof(ACC_X));
	char* acc_msg_y = (char * ) malloc(sizeof(ACC_Y));
	char* acc_msg_z = (char * ) malloc(sizeof(ACC_Z));
	sprintf(acc_msg_x, "%.1f", ACC_X);
	sprintf(acc_msg_y, "%.1f", ACC_Y);
	sprintf(acc_msg_z, "%.1f", ACC_Z);

	char* gyr_msg_x = (char * ) malloc(sizeof(GYR_X));
	char* gyr_msg_y = (char * ) malloc(sizeof(GYR_Y));
	char* gyr_msg_z = (char * ) malloc(sizeof(GYR_Z));
	sprintf(gyr_msg_x, "%.1f", GYR_X);
	sprintf(gyr_msg_y, "%.1f", GYR_Y);
	sprintf(gyr_msg_z, "%.1f", GYR_Z);

	char* prs_msg = (char * ) malloc(sizeof(PRS));
	sprintf(prs_msg, "%.1f", PRS);

	// 심박수%x,y,z(가속도)%x,y,z(자이로)%압력
	strcat(a, hrm_msg);
	strcat(a, "%(");
	strcat(a, acc_msg_x);
	strcat(a, ", ");
	strcat(a, acc_msg_y);
	strcat(a, ", ");
	strcat(a, acc_msg_z);
	strcat(a, ")%(");
	strcat(a, gyr_msg_x);
	strcat(a, ", ");
	strcat(a, gyr_msg_y);
	strcat(a, ", ");
	strcat(a, gyr_msg_z);
	strcat(a, ")%");
	strcat(a, prs_msg);

	payload_length = strlen(a);

	dlog_print(DLOG_INFO, LOG_TAG, "My data: %s", a);
	sap_socket_send_data(priv_data.socket, HELLO_ACC_CHANNELID, payload_length, a);
	free(hrm_msg);
	free(acc_msg_x);
	free(acc_msg_y);
	free(acc_msg_z);
	free(gyr_msg_x);
	free(gyr_msg_y);
	free(gyr_msg_z);
	free(prs_msg);
}

static void on_service_connection_requested(sap_peer_agent_h peer_agent,
					    sap_socket_h socket,
					    sap_service_connection_result_e result,
					    void *user_data)
{
	priv_data.socket = socket;
	priv_data.peer_agent = peer_agent;

	sap_peer_agent_set_service_connection_terminated_cb
		(priv_data.peer_agent, on_service_connection_terminated, user_data);

	sap_socket_set_data_received_cb(socket, on_data_recieved, peer_agent);

	sap_peer_agent_accept_service_connection(peer_agent);

}

static void on_agent_initialized(sap_agent_h agent,
				 sap_agent_initialized_result_e result,
				 void *user_data)
{
	switch (result) {
	case SAP_AGENT_INITIALIZED_RESULT_SUCCESS:
		dlog_print(DLOG_INFO, TAG, "agent is initialized");

		sap_agent_set_service_connection_requested_cb(agent,
							      on_service_connection_requested,
							      NULL);

		priv_data.agent = agent;
		agent_created = TRUE;
		break;

	case SAP_AGENT_INITIALIZED_RESULT_DUPLICATED:
		dlog_print(DLOG_INFO, TAG, "duplicate registration");
		break;

	case SAP_AGENT_INITIALIZED_RESULT_INVALID_ARGUMENTS:
		dlog_print(DLOG_INFO, TAG, "invalid arguments");
		break;

	case SAP_AGENT_INITIALIZED_RESULT_INTERNAL_ERROR:
		dlog_print(DLOG_INFO, TAG, "internal sap error");
		break;

	default:
		dlog_print(DLOG_INFO, TAG, "unknown status (%d)", result);
		break;
	}

	dlog_print(DLOG_INFO, TAG, "agent initialized callback is over");

}

static void on_device_status_changed(sap_device_status_e status, sap_transport_type_e transport_type,
				     void *user_data)
{
	switch (transport_type) {
	case SAP_TRANSPORT_TYPE_BT:
		dlog_print(DLOG_INFO, TAG, "connectivity type(%d): bt", transport_type);
		break;

	case SAP_TRANSPORT_TYPE_BLE:
		dlog_print(DLOG_INFO, TAG, "connectivity type(%d): ble", transport_type);
		break;

	case SAP_TRANSPORT_TYPE_TCP:
		dlog_print(DLOG_INFO, TAG, "connectivity type(%d): tcp/ip", transport_type);
		break;

	case SAP_TRANSPORT_TYPE_USB:
		dlog_print(DLOG_INFO, TAG, "connectivity type(%d): usb", transport_type);
		break;

	case SAP_TRANSPORT_TYPE_MOBILE:
		dlog_print(DLOG_INFO, TAG, "connectivity type(%d): mobile", transport_type);
		break;

	default:
		dlog_print(DLOG_INFO, TAG, "unknown connectivity type (%d)", transport_type);
		break;
	}

	switch (status) {
	case SAP_DEVICE_STATUS_DETACHED:

		if (priv_data.peer_agent) {
			sap_socket_destroy(priv_data.socket);
			priv_data.socket = NULL;
			sap_peer_agent_destroy(priv_data.peer_agent);
			priv_data.peer_agent = NULL;

		}

		break;

	case SAP_DEVICE_STATUS_ATTACHED:
		break;

	default:
		dlog_print(DLOG_INFO, TAG, "unknown status (%d)", status);
		break;
	}
}

gboolean agent_initialize()
{
	int result = 0;

	do {
		result = sap_agent_initialize(priv_data.agent, HELLO_ACC_ASPID, SAP_AGENT_ROLE_PROVIDER,
					      on_agent_initialized, NULL);

		dlog_print(DLOG_INFO, TAG, "SAP >>> getRegisteredServiceAgent() >>> %d", result);
	} while (result != SAP_RESULT_SUCCESS);

	return TRUE;
}

void initialize_sap()
{
	sap_agent_h agent = NULL;

	sap_agent_create(&agent);

	if (agent == NULL)
		dlog_print(DLOG_ERROR, TAG, "ERROR in creating agent");
	else
		dlog_print(DLOG_INFO, TAG, "SUCCESSFULLY create sap agent");

	priv_data.agent = agent;

	sap_set_device_status_changed_cb(on_device_status_changed, NULL);

	agent_initialize();
}
