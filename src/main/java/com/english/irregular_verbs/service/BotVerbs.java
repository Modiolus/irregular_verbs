package com.english.irregular_verbs.service;

import com.english.irregular_verbs.model.IrregularVerb;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface BotVerbs {

    void suggestionLink(long chatID);

    void donateLink(long chatId);

    void searchVerb(List<IrregularVerb> verbs, String messageText, long chatId);

    void registerUser(Message msg);

    void sendMessage(long chatId, String textToSend);

    void executeMessage(SendMessage message);

}
