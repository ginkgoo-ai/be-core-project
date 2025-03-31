package com.ginkgooai.core.project.service.application;

import com.ginkgooai.core.common.constant.MessageQueue;
import com.ginkgooai.core.common.message.InnerMailSendMessage;
import com.ginkgooai.core.common.queue.QueueInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SendEmailInnerService {

    private final QueueInterface queueInterface;

    public void email(InnerMailSendMessage message){
        queueInterface.send(MessageQueue.EMAIL_SEND_QUEUE, message);
    }
}
