#!/usr/bin/env python3
"""
Test script for List Mailboxes API
Usage: python3 test_list_mailboxes.py
"""

import requests
import json
import sys

BASE_URL = 'http://localhost:8080'
JWT_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDkwMjQ5NzQ0NjEzODA5MjUzOTkiLCJpYXQiOjE3MjY0MTU5MzMsImV4cCI6MTcyNjUwMjMzM30.Jlj0vGWe_Tz-QhYd8l4YJ4_XdCr2mO4I66Uu6OX0vbc'

def test_list_mailboxes():
    print('🧪 Testing List Mailboxes API')
    print('==============================')
    
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {JWT_TOKEN}'
    }
    
    try:
        # Test 1: List mailboxes with authentication
        print('\n📋 1. Testing GET /api/mailboxes (with auth)')
        print('----------------------------------------------')
        
        response = requests.get(f'{BASE_URL}/api/mailboxes', headers=headers)
        print(f'Status: {response.status_code}')
        
        if response.status_code == 200:
            data = response.json()
            print('Response:', json.dumps(data, indent=2))
            
            # Test 2: Create a mailbox if none exist
            if isinstance(data, list) and len(data) == 0:
                print('\n📊 2. No mailboxes found, creating one...')
                print('-------------------------------------------')
                
                create_data = {
                    'duration': 1,
                    'burnAfterRead': False
                }
                
                create_response = requests.post(
                    f'{BASE_URL}/api/mailboxes/create',
                    headers=headers,
                    json=create_data
                )
                
                print(f'Create Status: {create_response.status_code}')
                if create_response.status_code == 200:
                    create_result = create_response.json()
                    print('Create Response:', json.dumps(create_result, indent=2))
                    
                    # Test 3: List mailboxes again
                    print('\n📋 3. Testing GET /api/mailboxes again')
                    print('--------------------------------------')
                    
                    response2 = requests.get(f'{BASE_URL}/api/mailboxes', headers=headers)
                    print(f'Status: {response2.status_code}')
                    
                    if response2.status_code == 200:
                        data2 = response2.json()
                        print('Response:', json.dumps(data2, indent=2))
                else:
                    print('Create Response:', create_response.text)
        else:
            print('Response:', response.text)
        
        # Test 4: Test without authentication
        print('\n🔑 4. Testing without authentication')
        print('------------------------------------')
        
        unauth_headers = {'Content-Type': 'application/json'}
        unauth_response = requests.get(f'{BASE_URL}/api/mailboxes', headers=unauth_headers)
        
        print(f'Unauth Status: {unauth_response.status_code}')
        if unauth_response.status_code == 401:
            print('Expected 401 - Authentication required ✅')
        else:
            print('Response:', unauth_response.text)
        
        print('\n✅ All tests completed!')
        
    except requests.exceptions.ConnectionError:
        print('❌ Connection failed. Make sure the server is running on http://localhost:8080')
        sys.exit(1)
    except Exception as e:
        print(f'❌ Test failed: {str(e)}')
        sys.exit(1)

if __name__ == '__main__':
    test_list_mailboxes()