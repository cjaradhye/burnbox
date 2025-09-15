// Test script for List Mailboxes API
// Usage: node test_list_mailboxes.js

const BASE_URL = 'http://localhost:8080';
const JWT_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDkwMjQ5NzQ0NjEzODA5MjUzOTkiLCJpYXQiOjE3MjY0MTU5MzMsImV4cCI6MTcyNjUwMjMzM30.Jlj0vGWe_Tz-QhYd8l4YJ4_XdCr2mO4I66Uu6OX0vbc';

async function testListMailboxes() {
    console.log('🧪 Testing List Mailboxes API');
    console.log('==============================');

    try {
        // Test 1: List mailboxes with authentication
        console.log('\n📋 1. Testing GET /api/mailboxes (with auth)');
        console.log('----------------------------------------------');
        
        const response = await fetch(`${BASE_URL}/api/mailboxes`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${JWT_TOKEN}`
            }
        });

        console.log(`Status: ${response.status}`);
        const data = await response.json();
        console.log('Response:', JSON.stringify(data, null, 2));

        // Test 2: Create a mailbox if none exist
        if (Array.isArray(data) && data.length === 0) {
            console.log('\n📊 2. No mailboxes found, creating one...');
            console.log('-------------------------------------------');
            
            const createResponse = await fetch(`${BASE_URL}/api/mailboxes/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${JWT_TOKEN}`
                },
                body: JSON.stringify({
                    duration: 1,
                    burnAfterRead: false
                })
            });

            console.log(`Create Status: ${createResponse.status}`);
            const createData = await createResponse.json();
            console.log('Create Response:', JSON.stringify(createData, null, 2));

            // Test 3: List mailboxes again
            console.log('\n📋 3. Testing GET /api/mailboxes again');
            console.log('--------------------------------------');
            
            const response2 = await fetch(`${BASE_URL}/api/mailboxes`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${JWT_TOKEN}`
                }
            });

            console.log(`Status: ${response2.status}`);
            const data2 = await response2.json();
            console.log('Response:', JSON.stringify(data2, null, 2));
        }

        // Test 4: Test without authentication
        console.log('\n🔑 4. Testing without authentication');
        console.log('------------------------------------');
        
        const unauthResponse = await fetch(`${BASE_URL}/api/mailboxes`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        console.log(`Unauth Status: ${unauthResponse.status}`);
        if (unauthResponse.status !== 200) {
            console.log('Expected 401 - Authentication required ✅');
        }

        console.log('\n✅ All tests completed!');

    } catch (error) {
        console.error('❌ Test failed:', error.message);
    }
}

// Run the tests
testListMailboxes();