/*
    Copyright 2007-2010 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package evyframework.container.script;

import java.util.List;
import java.util.ArrayList;

/**

 */
public class ObjectPool <T>{

    protected List<T> free  = new ArrayList<T>();
    protected List<T> taken = new ArrayList<T>();

    protected int tokensTaken = 0;
    protected int maxSize     = 0;



    public T take(){
        this.tokensTaken++;
        T token = null;
        if(free.size() > 0) {
            token = free.remove(0);
        } else {
           // token =
        }
        taken.add(token);

        if(size() > maxSize){
            maxSize = size();
        }

        return token;
    }

    public void freeAll(){
        free.addAll(taken);
        taken.clear();
    }

    public void correctIndexOfTakenTokens(int valueToSubstract){
//        for(T token : this.taken){
//            if(token.length > 0){
//                token.from -= valueToSubstract;
//            }
//        }
    }

    public int size() {
        return free.size() + taken.size();
    }

    
}
