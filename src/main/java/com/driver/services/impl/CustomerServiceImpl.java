package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		Driver driver=null;
		TripBooking bookedTrip=new TripBooking();
		List<Driver> listOfDrivers=driverRepository2.findAll();
		for(Driver d: listOfDrivers){

			if(d.getCab().getAvailable()){
				if((driver==null) || driver.getDriverId()>d.getDriverId()){
					driver=d;

				}
			}
		}
		if(driver==null)
			throw new Exception("No cab available!");
		Customer customer=customerRepository2.findById(customerId).get();

		bookedTrip.setCustomer(customer);
		driver.getCab().setAvailable(false);
		bookedTrip.setDriver(driver);
		bookedTrip.setFromLocation(fromLocation);
		bookedTrip.setToLocation(toLocation);
		bookedTrip.setDistanceInKm(distanceInKm);
		bookedTrip.setStatus(TripStatus.CONFIRMED);

		customer.getTripBookingList().add(bookedTrip);
		customerRepository2.save(customer);

		driver.getTripBookingList().add(bookedTrip);
		driverRepository2.save(driver);
		return bookedTrip;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking currTrip=tripBookingRepository2.findById(tripId).get();
		currTrip.setStatus(TripStatus.CANCELED);
		currTrip.getDriver().getCab().setAvailable(true);
		currTrip.setBill(0);
		tripBookingRepository2.save(currTrip);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking currTrip=tripBookingRepository2.findById(tripId).get();
		currTrip.setStatus(TripStatus.COMPLETED);
		currTrip.getDriver().getCab().setAvailable(true);

		int dist=currTrip.getDistanceInKm();
		int cabPrice=currTrip.getDriver().getCab().getPerKmRate();
		int totalPrice=cabPrice*dist;
		currTrip.setBill(totalPrice);
		tripBookingRepository2.save(currTrip);

	}
}
