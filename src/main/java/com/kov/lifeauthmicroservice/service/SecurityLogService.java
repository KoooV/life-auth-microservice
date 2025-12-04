package com.kov.lifeauthmicroservice.service;

import com.kov.lifeauthmicroservice.model.SecurityEvent;
import com.kov.lifeauthmicroservice.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.net.UnknownHostException;

import java.net.InetAddress;

@RequiredArgsConstructor
@Service
public class SecurityLogService {


    private final SecurityLogService securityLogService;

    public void securityLog(User user, SecurityEvent eventType, HttpServletRequest request){


    }

    public String getIp(HttpServletRequest request){
        return request.getRemoteAddr();
    }

    public boolean isValidIp(String ip){
        try{
            InetAddress.getByName(ip);
            return true;
        }catch (UnknownHostException e){
            return false;
        }
    }

//    public String getUserAgent(HttpServletRequest request){
//
//    }


}
