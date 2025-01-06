package edu.uclm.esi.listasbe.ws;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;

@Component
public class wsListas extends TextWebSocketHandler {
    
    @Autowired
    private static ListaDao listaDao;
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();

    public void difundir(JSONObject json) throws IOException {
        TextMessage message = new TextMessage(json.toString());
        for (WebSocketSession target : this.sessions.values()) {
            new Thread(() -> {
                try {
                    target.sendMessage(message);
                } catch (IOException e) {
                    wsListas.this.sessions.remove(target.getId());
                }
            }).start();
        }
    }
    
    @Autowired
    public void setlistaDao(ListaDao listadao) {
        wsListas.listaDao = listadao;
    }

    private String getParameter(WebSocketSession session, String parName) {
        URI uri = session.getUri();
        String query = uri.getQuery();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && parName.equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session.getId());
        String email = this.getParameter(session, "email");
        
        List<String> listas = this.listaDao.getListasDe(email);
        for (String idLista : listas) {
            List<WebSocketSession> auxi = this.sessionsByIdLista.get(idLista);
            if (auxi == null) {
                auxi = new ArrayList<>();
                auxi.add(session);
            } else {
                auxi.add(session);
            }
            this.sessionsByIdLista.put(idLista, auxi);
        }
    }

    public void notificar(String idLista, Producto producto) {
        System.out.println("Notificando");
        JSONObject jso = new JSONObject();
        jso.put("tipo", "actualizacionDeLista");
        jso.put("idLista", idLista);
        jso.put("id", producto.getId());
        jso.put("unidadesCompradas", producto.getUnidadesCompradas());
        jso.put("unidadesPedidas", producto.getUnidadesPedidas());
        jso.put("nombre", producto.getNombre());
        
        TextMessage message = new TextMessage(jso.toString());
        System.out.println("Notificando a " + idLista);
        System.out.println(this.sessionsByIdLista + " sessions by lista");
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        System.out.println("Interesados: " + interesados);
        if (interesados != null) {
            Iterator<WebSocketSession> iterator = interesados.iterator();
            while (iterator.hasNext()) {
                WebSocketSession target = iterator.next();
                if (target.isOpen()) {
                    new Thread(() -> {
                        try {
                            target.sendMessage(message);
                        } catch (IOException e) {
                            System.err.println("Error sending message: " + e.getMessage());
                        }
                    }).start();
                } else {
                    System.out.println("Session is closed: " + target.getId());
                    iterator.remove(); // Safely remove the closed session
                }
            }
        } else {
            System.out.println("No interested sessions for list: " + idLista);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Handle transport error
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, Throwable exception) throws Exception {
        this.sessions.remove(session.getId());
        // Remove the session from sessionsByIdLista
        for (List<WebSocketSession> sessionList : this.sessionsByIdLista.values()) {
            sessionList.remove(session);
        }
    }
}