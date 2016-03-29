(function() {
    window.WallTime || (window.WallTime = {});
    window.WallTime.data = {
        rules: {"US":[{"name":"US","_from":"1918","_to":"1919","type":"-","in":"Mar","on":"lastSun","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"1918","_to":"1919","type":"-","in":"Oct","on":"lastSun","at":"2:00","_save":"0","letter":"S"},{"name":"US","_from":"1942","_to":"only","type":"-","in":"Feb","on":"9","at":"2:00","_save":"1:00","letter":"W"},{"name":"US","_from":"1945","_to":"only","type":"-","in":"Aug","on":"14","at":"23:00u","_save":"1:00","letter":"P"},{"name":"US","_from":"1945","_to":"only","type":"-","in":"Sep","on":"lastSun","at":"2:00","_save":"0","letter":"S"},{"name":"US","_from":"1967","_to":"2006","type":"-","in":"Oct","on":"lastSun","at":"2:00","_save":"0","letter":"S"},{"name":"US","_from":"1967","_to":"1973","type":"-","in":"Apr","on":"lastSun","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"1974","_to":"only","type":"-","in":"Jan","on":"6","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"1975","_to":"only","type":"-","in":"Feb","on":"23","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"1976","_to":"1986","type":"-","in":"Apr","on":"lastSun","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"1987","_to":"2006","type":"-","in":"Apr","on":"Sun>=1","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"2007","_to":"max","type":"-","in":"Mar","on":"Sun>=8","at":"2:00","_save":"1:00","letter":"D"},{"name":"US","_from":"2007","_to":"max","type":"-","in":"Nov","on":"Sun>=1","at":"2:00","_save":"0","letter":"S"}],"Chicago":[{"name":"Chicago","_from":"1920","_to":"only","type":"-","in":"Jun","on":"13","at":"2:00","_save":"1:00","letter":"D"},{"name":"Chicago","_from":"1920","_to":"1921","type":"-","in":"Oct","on":"lastSun","at":"2:00","_save":"0","letter":"S"},{"name":"Chicago","_from":"1921","_to":"only","type":"-","in":"Mar","on":"lastSun","at":"2:00","_save":"1:00","letter":"D"},{"name":"Chicago","_from":"1922","_to":"1966","type":"-","in":"Apr","on":"lastSun","at":"2:00","_save":"1:00","letter":"D"},{"name":"Chicago","_from":"1922","_to":"1954","type":"-","in":"Sep","on":"lastSun","at":"2:00","_save":"0","letter":"S"},{"name":"Chicago","_from":"1955","_to":"1966","type":"-","in":"Oct","on":"lastSun","at":"2:00","_save":"0","letter":"S"}]},
        zones: {"America/Chicago":[{"name":"America/Chicago","_offset":"-5:50:36","_rule":"-","format":"LMT","_until":"1883 Nov 18 12:09:24"},{"name":"America/Chicago","_offset":"-6:00","_rule":"US","format":"C%sT","_until":"1920"},{"name":"America/Chicago","_offset":"-6:00","_rule":"Chicago","format":"C%sT","_until":"1936 Mar 1 2:00"},{"name":"America/Chicago","_offset":"-5:00","_rule":"-","format":"EST","_until":"1936 Nov 15 2:00"},{"name":"America/Chicago","_offset":"-6:00","_rule":"Chicago","format":"C%sT","_until":"1942"},{"name":"America/Chicago","_offset":"-6:00","_rule":"US","format":"C%sT","_until":"1946"},{"name":"America/Chicago","_offset":"-6:00","_rule":"Chicago","format":"C%sT","_until":"1967"},{"name":"America/Chicago","_offset":"-6:00","_rule":"US","format":"C%sT","_until":""}]}
    };
    window.WallTime.autoinit = true;
}).call(this);
