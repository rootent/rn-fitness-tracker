import React from 'react';
import { View,TouchableOpacity, Text, StyleSheet } from 'react-native';

import RNFitnessTracker from '@kilohealth/rn-fitness-tracker';

class App extends React.Component {

  state = {
    authorized: false,
    steps: 0,
    stepsThisWeek: 0,
    dailyStepsThisWeek: {},
  };

  render() {
    return (
        <View style={{marginTop: 100}}>
            <TouchableOpacity
              style={styles.authorize}
              onPress={() => {
                if (RNFitnessTracker) {
                    RNFitnessTracker.authorize(state => {
                    this.setState({ authorized: state });
                  });
                } else {
                  alert('fitness tracker not defined');
                }
              }}>
            <Text>AUTHORIZE</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.getSteps}
              onPress={() => RNFitnessTracker.getStepsToday(steps => {
                  this.setState({steps: steps})
              })}>
            <Text>Get Steps</Text>
            </TouchableOpacity>
            <TouchableOpacity
                style={styles.getSteps}
                onPress={() => RNFitnessTracker.getWeekData(steps => {
                    this.setState({ stepsThisWeek: steps})
                })}>
                <Text>Get Steps This Week</Text>
            </TouchableOpacity>
            <TouchableOpacity
                style={styles.getSteps}
                onPress={() => RNFitnessTracker.getDailyWeekData(steps => {
                    this.setState({ dailyStepsThisWeek: steps})
                })}>
                <Text>Get Daily Steps This Week</Text>
            </TouchableOpacity>
            <Text>{ this.state.authorized ? ' authorized' : ' not authorized' }</Text>
            <Text>{` steps: ${this.state.steps}`}</Text>
            <Text>{` steps this week: ${this.state.stepsThisWeek.toString()}`}</Text>
            <Text>{` daily steps this week: ${JSON.stringify(this.state.dailyStepsThisWeek)}`}</Text>
            <Text></Text>
        </View>
        );
    }

};

const styles = StyleSheet.create({

  authorize: {
    padding: 15,
    width: '100%',
    marginBottom: 10,
    marginTop: 10,
    backgroundColor: 'red'
  },
  getSteps: {
    padding: 15,
    width: '100%',
    marginBottom: 10,
    marginTop: 10,
    backgroundColor: 'green'
  }
});

export default App;
