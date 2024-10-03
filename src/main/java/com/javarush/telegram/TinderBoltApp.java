package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

import static java.awt.SystemColor.text;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "Love_like_my_Bot";
    public static final String TELEGRAM_BOT_TOKEN = "7102285928:AAFlx2ZZQM1SFrFz-KqLJY2npHK-w5CMNuQ";
    public static final String OPEN_AI_TOKEN = "gpt:4dws6NYyD0BDK2ufp71ZJFkblB3TCC3tppbmX6OYmhSFydbM";

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo she;
    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String messageText = getMessageText();

        if (messageText.equals("/start")) {
            currentMode = DialogMode.MAIN;

            sendPhotoMessage("main");

            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder-профля", "/profile",
                    "сообщение для знакомства", "/opener",
                    "переписка от вашего имен", "/message",
                    "переписка со звездами", "/date",
                    "задать вопрос ChatGPT", "/gpt");
            return;
        }

        if (messageText.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("Генерация ответа...");
            String answer = chatGPT.sendMessage(prompt, messageText);
            updateTextMessage(msg, answer);
            return;
        }

        if (messageText.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            String quary = getCallbackQueryButtonKey();

            if (quary.startsWith("date_")) {
                sendPhotoMessage(quary);
                sendTextMessage("Отличный выбор!");
                String prompt = loadPrompt(quary);
                chatGPT.setPrompt(prompt);
                return;
            }

            String answer = chatGPT.addMessage(messageText);
            sendTextMessage(answer);
            return;
        }

        if (messageText.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            //String text = loadMessage("gpt");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")){
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Генерация ответа...");
                String answer = chatGPT.sendMessage(prompt, messageText);
                updateTextMessage(msg, answer);
                return;
            }
            list.add(messageText);
            return;
        }

        if (messageText.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько вам лет?");
            return;
        }

        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    me.age = messageText;
                    questionCount = 2;
                    sendTextMessage("Кем Вы работаете?");
                    return;
                case 2:
                    me.occupation = messageText;
                    questionCount = 3;
                    sendTextMessage("Есть ли у Вас хобби?");
                    return;
                case 3:
                    me.hobby = messageText;
                    questionCount = 4;
                    sendTextMessage("Что Вам НЕ нравится в людях?");
                    return;
                case 4:
                    me.annoys = messageText;
                    questionCount = 5;
                    sendTextMessage("Цели знакомства?");
                    return;
                case 5:
                    me.goals = messageText;

                    String aboutMe = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Генерация ответа...");
                    String answer = chatGPT.sendMessage(prompt, aboutMe);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        if (messageText.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя девушки?");
            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    she.name = messageText;
                    questionCount = 2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age = messageText;
                    questionCount = 3;
                    sendTextMessage("Есть ли у неё хобби?");
                    return;
                case 3:
                    she.hobby = messageText;
                    questionCount = 4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation = messageText;
                    questionCount = 5;
                    sendTextMessage("Цели знакомства?");
                    return;
                case 5:
                    she.goals = messageText;

                    String aboutFriend = she.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Генерация ответа...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        sendTextMessage("Выберите режим работы");
    }


        public static void main (String[]args) throws TelegramApiException {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TinderBoltApp());
        }
    }