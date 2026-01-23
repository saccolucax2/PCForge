package it.unisannio.chat.presentation;

import it.unisannio.chat.data.Conversation;
import it.unisannio.chat.data.Message;
import it.unisannio.chat.application.ChatService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    private final ChatService chatService;

    public ChatController() {
        // Usa i singleton dei repository direttamente
        this.chatService = new ChatService();
    }

    // 🔹 Crea una nuova conversazione tra due utenti
    @POST
    @Path("/conversation")
    public Response createConversation(Map<String, String> body) {
        String user1 = body.get("user1Id");
        String user2 = body.get("user2Id");
        Conversation conv = chatService.createConversation(user1, user2);
        return Response.ok(conv).build();
    }

    @GET
    @Path("/conversation/{conversationId}/partecipants")
    public Response getConversationParticipants(@PathParam("conversationId") String conversationId) {
        List<String> participants = chatService.getConversationParticipants(conversationId);
        return Response.ok(participants).build();
    }

    // 🔹 Ottiene tutte le conversazioni di un utente
    @GET
    @Path("/conversations/{userId}")
    public Response getConversations(@PathParam("userId") String userId) {
        List<Conversation> list = chatService.getConversationsForUser(userId);
        return Response.ok(list).build();
    }

    // 🔹 Ottiene tutti i messaggi di una conversazione
    @GET
    @Path("/messages/{conversationId}")
    public Response getMessages(@PathParam("conversationId") String conversationId) {
        List<Message> messages = chatService.getMessages(conversationId);
        return Response.ok(messages).build();
    }

    // 🔹 Invia un messaggio
    @POST
    @Path("/message")
    public Response sendMessage(Map<String, String> body) {
        String conversationId = body.get("conversationId");
        String fromUserId = body.get("fromUserId");
        String content = body.get("content");
        Message msg = chatService.sendMessage(conversationId, fromUserId, content);
        return Response.ok(msg).build();
    }

    // 🔹 Segna tutti i messaggi come letti
    @PUT
    @Path("/conversation/{conversationId}/read/{userId}")
    public Response markAsRead(@PathParam("conversationId") String conversationId,
                               @PathParam("userId") String userId) {
        chatService.markMessagesAsRead(conversationId, userId);
        return Response.ok().build();
    }

    // 🔹 Conta i messaggi non letti per un utente
    @GET
    @Path("/conversation/{conversationId}/unread/{userId}")
    public Response countUnread(@PathParam("conversationId") String conversationId,
                                @PathParam("userId") String userId) {
        long count = chatService.countUnreadMessages(conversationId, userId);
        return Response.ok(Map.of("unreadCount", count)).build();
    }

    // 🔹 Cancella una conversazione e i messaggi collegati
    @DELETE
    @Path("/conversation/{conversationId}")
    public Response deleteConversation(@PathParam("conversationId") String conversationId) {
        boolean deleted = chatService.deleteConversation(conversationId);
        return deleted ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

}