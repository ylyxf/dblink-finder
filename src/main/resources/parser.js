function parse(content,dblinks,url,user){
    var result = {
        dblinkTables:[],
        notDblinkTables:[],
        test:[]
    };

    if (content.indexOf("@") == -1) {
        return result;
    }

    content = content.replace(/\r@/g, "@");
    content = content.replace(/\n@/g, "@");
    content = content.replace(/\r\n@/g, "@");
    content = content.replace(/\r/g, " ");
    content = content.replace(/\n/g, " ");
    content = content.replace(/\t/g, " ");
    content = content.replace(/,/g, " , ");

    var segments = content.split(" ");
    for (var segmentIndex in segments) {
        var segment = segments[segmentIndex];
        if (segment.indexOf("@") == -1) {
            continue;
        }
        
        var subSegs = segment.split("@");
        var left = subSegs[0];
        var right = subSegs[1];

        var rightContainsDblink = false;
        for(var dblinkIndex in dblinks){
            var dblinkName =  dblinks[dblinkIndex];
            result.test.push(dblinkName);
        }

        if(!rightContainsDblink){
            result.notDblinkTables.push(segment);
            continue;
        }
        if(left.lastIndexOf(',')!=-1){
            left= left.substr(left.lastIndexOf(',')+1)
        }

        if(left.lastIndexOf('=')!=-1){
            left= left.substr(left.lastIndexOf('=')+1)
        }

        if(left.lastIndexOf("'")!=-1){
            left= left.substr(left.lastIndexOf("'")+1)
        }

        if(left.lastIndexOf('(')!=-1){
            left= left.substr(left.lastIndexOf('(')+1)
        }

        
        if(left.lastIndexOf("'")){
            left= left.substr(left.lastIndexOf("'")+1)
        }

        if(left.lastIndexOf('!')!=-1){
            left= left.substr(left.lastIndexOf('!')+1)
        }

        if(left.lastIndexOf(';')!=-1){
            left= left.substr(left.lastIndexOf(';')+1)
        }

        if(left.lastIndexOf('%')!=-1){
            left= left.substr(left.lastIndexOf('%')+1)
        }

        if(left.lastIndexOf(':')!=-1){
            left= left.substr(left.lastIndexOf(':')+1)
        }

        left = left.trim();
        if(left == ''){
            result.notDblinkTables.push(segment);
            continue;
        }

        if(left.lastIndexOf('.')==-1){
            result.notDblinkTables.push(segment);
            continue;
        }

        //-----

        if(right.indexOf(',')!=-1){
            right = right.substr(0,right.indexOf(','));
        }

        if(right.indexOf("'")!=-1){
            right = right.substr(0,right.indexOf("'"));
        }

        if(right.indexOf('.')!=-1){
            right = right.substr(0,right.indexOf('.'));
        }


        if(right.indexOf('(')!=-1){
            right = right.substr(0,right.indexOf('('));
        }

        if(right.indexOf(')')!=-1){
            right = right.substr(0,right.indexOf(')'));
        }

        if(right.indexOf(';')!=-1){
            right = right.substr(0,right.indexOf(';'));
        }

        //---
        if(right.indexOf("--") != -1){
            result.notDblinkTables.push(segment);
            continue;
        }  

        if(right.indexOf("/*") != -1){
            result.notDblinkTables.push(segment);
            continue;
        }  

        if(right.indexOf("*/") != -1){
            result.notDblinkTables.push(segment);
            continue;
        }  

        right = right.trim();
        if(right == ''){
            result.notDblinkTables.push(segment);
            continue;
        }


        //----
        if(left.indexOf("--") != -1){
            result.notDblinkTables.push(segment);
            continue;
        }  

        if(left.indexOf("/*") != -1){
            result.notDblinkTables.push(segment);
            continue;
        }  

        if(left.indexOf("*/") != -1){
            result.notDblinkTables.push(segment);
            continue;
        }  

        var dblinktable = left+'@'+right;
        result.dblinkTables.push(dblinktable);
    }

    return result;
}