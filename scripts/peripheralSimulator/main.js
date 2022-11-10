const bleno = require('bleno');
const BlenoPrimaryService = bleno.PrimaryService;

const name = 'mosip try 1';
const serviceUuids = ['fffffffffffffffffffffffffffffff0']

bleno.on('stateChange', function(state) {
    console.log('on -> stateChange: ' + state);

    if (state === 'poweredOn') {
        bleno.startAdvertising(name, serviceUuids);
    } else {
        bleno.stopAdvertising();
    }
});

bleno.on('advertisingStart', function(error) {
    console.log('on -> advertisingStart: ' + (error ? 'error ' + error : 'success'));

    if (!error) {
        bleno.setServices([
            new BlenoPrimaryService({
                uuid: 'ab25',
                characteristics: [characteristic]
            })
        ]);
    }
});

const characteristic = new bleno.Characteristic({
    uuid: 'fff1', // or 'fff1' for 16-bit
    properties: ['read', 'write', 'writeWithoutResponse'], // can be a combination of 'read', 'write', 'writeWithoutResponse', 'notify', 'indicate'
    secure: [], // enable security for properties, can be a combination of 'read', 'write', 'writeWithoutResponse', 'notify', 'indicate'
    value: "", // optional static value, must be of type Buffer - for read only characteristics
    descriptors: [],
    onReadRequest: function(offset, callback) {
        if(!this.textInUpperCase) {
            this.textInUpperCase = "DEFAULT VALUE"
        }

        callback(this.RESULT_SUCCESS, Buffer.from(this.textInUpperCase));
    },
    onWriteRequest: function(data, offset, withoutResponse, callback) {
        console.log("Received write request with data: " + data);
        this.textInUpperCase = data.toString().toUpperCase();

        callback(this.RESULT_SUCCESS);
    }, // optional write request handler, function(data, offset, withoutResponse, callback) { ...}
    onSubscribe: null, // optional notify/indicate subscribe handler, function(maxValueSize, updateValueCallback) { ...}
    onUnsubscribe: null, // optional notify/indicate unsubscribe handler, function() { ...}
    onNotify: null, // optional notify sent handler, function() { ...}
    onIndicate: null // optional indicate confirmation received handler, function() { ...}
});
