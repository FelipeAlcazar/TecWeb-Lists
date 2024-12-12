package edu.uclm.esi.listasbe.ws;


import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class wsChat extends TextWebSocketHandler {

	private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private Map<String, WebSocketSession> sessionsByNombre = new ConcurrentHashMap<>();


	public void difundir(JSONObject json) throws IOException {
		TextMessage message=new TextMessage(json.toString());
		for(WebSocketSession target : this.sessions.values()) {
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						target.sendMessage(message);
					}catch (IOException e){
						wsChat.this.sessions.remove(target.getId());
					}
				}
			}).start();
		}
	}
	

	private String getNombreParameter(WebSocketSession session) {
		URI uri= session.getUri();
		String query = uri.getQuery();
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
			if (pair.length > 1 && "nombre".equals(pair[0])) {
				return pair[1];
			}
		}
		return null;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println(session.getId());
		String nombreUsuario = this.getNombreParameter(session);
		this.sessions.put(session.getId(), session);
		this.sessionsByNombre.put(nombreUsuario, session);
		JSONObject jso = new JSONObject();
		jso.put("tipo", "llegadaDelUsuario");
		jso.put("contenido", nombreUsuario);
		this.difundir(jso);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		if (jso.getString("tipo").equalsIgnoreCase("difusion")) {
			jso.put("tipo", "mensajeDeTexto");
			jso.put("contenido", jso.getString("contenido"));
			this.difundir(jso);
		}else if (jso.getString("tipo").equalsIgnoreCase("mensajeParticular")) {
			String destinatario = jso.getString("destinatario");
			WebSocketSession wsDestinatario = this.sessionsByNombre.get(destinatario);
			if(wsDestinatario != null) {
				wsDestinatario.sendMessage(message);
			}
		}else {
			// se hace algo como mandar un mensaje de error
		}
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
	}
	
	public void afterConnectionClosed(WebSocketSession session,  Throwable exception) throws Exception {
		this.sessions.remove(session.getId());
	}
	
}