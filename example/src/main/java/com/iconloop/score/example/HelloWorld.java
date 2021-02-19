/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;

public class HelloWorld implements HelloWorldInterface {
    private final String name;
    private String greeting = "Hello";
    private String to;

    public HelloWorld(String _name) {
        this.name = _name;
    }

    @External(readonly=true)
    public String name() {
        return name;
    }

    @External(readonly=true)
    public String greeting() {
        String msg = "["+name+"] "+greeting+" "+to+"!";
        Context.println(msg);
        return msg;
    }

    @External
    public void setGreeting(String _greeting) {
        this.greeting = _greeting;
    }

    @Payable
    @External
    public void setTo(String _to) {
        this.to = _to;
    }

    @Payable
    @External
    public void intercall(Address _address) {
        HelloWorldInterfaceImpl helloWorldInterfaceImpl = new HelloWorldInterfaceImpl(_address);
        String before = helloWorldInterfaceImpl.greeting();
        helloWorldInterfaceImpl._setICX(Context.getValue()).setTo(name);
        Intercall(_address, Context.getValue(), before, helloWorldInterfaceImpl.greeting());
    }

    @EventLog(indexed=1)
    public void Intercall(Address _address, BigInteger icx, String before, String after) {}

    @Payable
    public void fallback() {
        // just receive incoming funds
    }

}
