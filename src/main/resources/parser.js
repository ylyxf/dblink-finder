function parse(content, dblinks, url, user) {
    var result = {
        dblinkTables: [],
        notDblinkTables: []
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

        //suffix 
        var rightContainsDblink = false;
        dblinks.forEach(function (val) {
            if (right.toUpperCase().indexOf(val) == 0) {
                rightContainsDblink = true;
            }
        })
        if (!rightContainsDblink) {
            result.notDblinkTables.push("not valid dblink name after @:" + segment);
            continue;
        }

        var rightExcludeLetters = ["-",",", "(", ")", "'","/*","*/","="];
        rightExcludeLetters.forEach(function (excludeLetter) {
            if (right.indexOf(excludeLetter) != -1) {
                right = right.substr(0, right.indexOf(excludeLetter));
            }
        })

        var leftExcludeLetters = ["-",",", "(", ")", "'","/*","*/","="];
        leftExcludeLetters.forEach(function (excludeLetter) {
            if (left.lastIndexOf(excludeLetter) != -1) {
                left = left.substr(left.lastIndexOf(excludeLetter)+excludeLetter.length);
            }
        })

        if(left.trim().length==0){
            result.notDblinkTables.push("length is 0 before @ when replace leftExcludeLetters:  " + segment);
            continue;
        }

        var dblinktable = left + '@' + right;
        result.dblinkTables.push(dblinktable);
    }

    return result;
}