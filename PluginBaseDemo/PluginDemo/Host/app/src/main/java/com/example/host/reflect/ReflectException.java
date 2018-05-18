/*
 * Copyright (c) 2014, 张涛, lody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.host.reflect;

/**
 * 反射发生错误时抛出异常
 * Created by liming on 2018/5/15.
 */
public class ReflectException extends RuntimeException {

    private static final long serialVersionUID = -2243843843843438438L;

    public ReflectException(Throwable cause) {
        super(cause);
    }
}
