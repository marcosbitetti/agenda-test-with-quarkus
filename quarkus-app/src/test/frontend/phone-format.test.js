const test = require('node:test');
const assert = require('node:assert/strict');

const { formatPhoneNumberForDisplay } = require('../../main/resources/META-INF/resources/js/phone-format.js');

test('formats 11-digit mobile phone numbers for display', () => {
  assert.equal(formatPhoneNumberForDisplay('12988598514'), '(12) 98859-8514');
});

test('formats 10-digit landline phone numbers for display', () => {
  assert.equal(formatPhoneNumberForDisplay('1132654321'), '(11) 3265-4321');
});

test('keeps unsupported values unchanged', () => {
  assert.equal(formatPhoneNumberForDisplay('123456789'), '123456789');
});